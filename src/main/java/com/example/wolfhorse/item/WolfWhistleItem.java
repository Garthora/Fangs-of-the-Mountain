package com.example.wolfhorse.item;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Right-click one of your DireWolves with it: tags that DireWolf to this whistle.
 * Tap right-click: whistle, giving the tagged DireWolf the selected command.
 * Hold right-click: opens a radial menu (client side) to choose the command.
 */
public class WolfWhistleItem extends Item {
    /** Order here is the order around the radial menu, clockwise from the top. Ordinals are saved. */
    public enum Mode {
        // id, whistle pitch (each command sounds different)
        GO("go", 2.0F),          // highest: runs to the block you're looking at and waits there
        SIT("sit", 1.0F),        // low
        FOLLOW("follow", 1.7F),  // high
        GUARD("guard", 0.75F),   // lowest: stays where it is and fights hostile mobs nearby
        ATTACK("attack", 1.35F); // middle: attacks the mob you're looking at

        public final String key;
        public final float pitch;

        Mode(String key, float pitch) {
            this.key = key;
            this.pitch = pitch;
        }

        public Component label() {
            return Component.translatable("item.fotm.wolf_whistle.mode." + this.key);
        }
    }

    /** Released before this many ticks = a tap (server side). */
    public static final int TAP_MAX_TICKS = 6;
    /** Held this long = open the radial menu (client side). Kept above TAP_MAX_TICKS on purpose. */
    public static final int RADIAL_OPEN_TICKS = 8;

    private static final double GO_LOOK_RANGE = 64.0D;
    private static final double ATTACK_LOOK_RANGE = 48.0D;

    public WolfWhistleItem(Properties properties) {
        super(properties);
    }

    // ---------- stored data ----------

    public static Mode getMode(ItemStack stack) {
        int i = stack.hasTag() ? stack.getTag().getInt("Mode") : 0;
        Mode[] all = Mode.values();
        return all[Math.floorMod(i, all.length)];
    }

    /** Called from the radial menu's packet. */
    public static void selectMode(ItemStack stack, Player player, int index) {
        Mode[] all = Mode.values();
        if (index < 0 || index >= all.length) {
            return;
        }
        stack.getOrCreateTag().putInt("Mode", index);
        player.displayClientMessage(Component.translatable("message.fotm.whistle.mode",
                all[index].label().copy().withStyle(ChatFormatting.GOLD)), true);
    }

    // ---------- tagging a DireWolf ----------

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof WolfHorseEntity wolf)) {
            return InteractionResult.PASS;
        }
        Level level = player.level();
        if (!level.isClientSide) {
            if (wolf.isTamed() && wolf.isOwner(player)) {
                CompoundTag tag = player.getItemInHand(hand).getOrCreateTag();
                tag.putUUID("Wolf", wolf.getUUID());
                tag.putString("WolfName", wolf.getName().getString());
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 0.7F, 1.8F);
                player.displayClientMessage(Component.translatable("message.fotm.whistle.tagged",
                        wolf.getName()), true);
            } else {
                player.displayClientMessage(Component.translatable("message.fotm.whistle.not_yours"), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    // ---------- tap vs hold ----------

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.TOOT_HORN;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity user, int remaining) {
        int held = this.getUseDuration(stack) - remaining;
        if (held < TAP_MAX_TICKS && user instanceof Player player) {
            this.blow(stack, level, player);
        }
    }

    // ---------- the commands ----------

    private void blow(ItemStack stack, Level level, Player player) {
        Mode mode = getMode(stack);
        player.getCooldowns().addCooldown(this, 12);

        if (!(level instanceof ServerLevel server)) {
            return;
        }
        WhistleSounds.play(server, player, mode.pitch);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.hasUUID("Wolf")) {
            say(player, "no_tag");
            return;
        }
        Entity found = server.getEntity(tag.getUUID("Wolf"));
        if (!(found instanceof WolfHorseEntity wolf) || !wolf.isAlive() || !wolf.isOwner(player)) {
            String name = tag.getString("WolfName");
            say(player, "not_found", Component.literal(name.isEmpty() ? "Your DireWolf" : name));
            return;
        }
        Component name = wolf.getName();
        tag.putString("WolfName", name.getString());
        if (wolf.isVehicle()) {
            say(player, "ridden", name);
            return;
        }

        switch (mode) {
            case GO -> {
                BlockPos spot = findLookSpot(player);
                if (spot == null) {
                    say(player, "go_none", name);
                    return;
                }
                wolf.setOrderedToSit(false);
                wolf.setTarget(null);
                wolf.goTo(spot);
                say(player, "go", name);
            }
            case SIT -> {
                wolf.stopGuarding();
                wolf.setOrderedToSit(true);
                wolf.setTarget(null);
                wolf.getNavigation().stop();
                say(player, "sit", name);
            }
            case FOLLOW -> {
                wolf.stopGuarding();
                wolf.setOrderedToSit(false);
                say(player, "follow", name);
            }
            case GUARD -> {
                wolf.setOrderedToSit(false);
                wolf.setTarget(null);
                wolf.setGuarding(wolf.blockPosition());
                say(player, "guard", name);
            }
            case ATTACK -> {
                LivingEntity target = findLookTarget(player, wolf);
                if (target == null || !wolf.canAttack(target) || !wolf.wantsToAttack(target, player)) {
                    say(player, "attack_none", name);
                    return;
                }
                wolf.setOrderedToSit(false);
                wolf.stopGuarding();
                wolf.setTarget(target);
                say(player, "attack", name, target.getName());
            }
        }
    }

    private static void say(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("message.fotm.whistle." + key, args), true);
    }

    /** The open spot next to the block the player is looking at, up to 64 blocks away. */
    @Nullable
    private static BlockPos findLookSpot(Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(GO_LOOK_RANGE));
        BlockHitResult hit = player.level().clip(
                new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, player));
        if (hit.getType() == HitResult.Type.MISS) {
            return null;
        }
        return hit.getBlockPos().relative(hit.getDirection());
    }

    /** The living thing the player is looking at, up to 48 blocks away, not through walls. */
    @Nullable
    private static LivingEntity findLookTarget(Player player, WolfHorseEntity wolf) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(ATTACK_LOOK_RANGE));
        BlockHitResult block = player.level().clip(
                new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (block.getType() != HitResult.Type.MISS) {
            end = block.getLocation();
        }
        AABB box = player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1.0D);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, end, box,
                e -> e instanceof LivingEntity && e != wolf && !e.isSpectator() && e.isPickable(),
                eye.distanceToSqr(end));
        return hit != null ? (LivingEntity) hit.getEntity() : null;
    }

    // ---------- tooltip ----------

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.hasUUID("Wolf")) {
            tooltip.add(Component.translatable("item.fotm.wolf_whistle.tagged",
                    Component.literal(tag.getString("WolfName")).withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.translatable("item.fotm.wolf_whistle.untagged").withStyle(ChatFormatting.GRAY));
        }
        tooltip.add(Component.translatable("message.fotm.whistle.mode",
                getMode(stack).label().copy().withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.fotm.wolf_whistle.help.tag").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.fotm.wolf_whistle.help.tap").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.fotm.wolf_whistle.help.hold").withStyle(ChatFormatting.DARK_GRAY));
    }
}
