package com.example.wolfhorse.item;

import com.example.wolfhorse.OldIdMigration;
import com.example.wolfhorse.WolfHorseConfig;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Carries one of your tamed DireWolves.
 * Right-click your DireWolf: it goes inside (with its saddle, armor, chest, name and stats).
 * Right-click a block: it comes back out there. The same DireWolf comes back, so whistles still work.
 */
public class DireWolfBackpackItem extends Item {
    private static final String WOLF = "DireWolf";
    private static final String NAME = "DireWolfName";

    public DireWolfBackpackItem(Properties properties) {
        super(properties);
    }

    public static boolean hasWolf(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(WOLF);
    }

    private static void say(Player player, String key, Object... args) {
        player.displayClientMessage(Component.translatable("message.fotm.backpack." + key, args), true);
    }

    // ---------- putting a DireWolf in ----------

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        if (!(target instanceof WolfHorseEntity wolf)) {
            return InteractionResult.PASS;
        }
        Level level = player.level();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack held = player.getItemInHand(hand); // the real stack, also in creative
        if (hasWolf(held)) {
            say(player, "full");
        } else if (!wolf.isTamed() || !wolf.isOwner(player)) {
            say(player, "not_yours");
        } else if (wolf.isVehicle() || wolf.isPassenger()) {
            say(player, "ridden", wolf.getName());
        } else {
            if (wolf.isLeashed()) {
                wolf.dropLeash(true, !player.getAbilities().instabuild);
            }
            CompoundTag data = new CompoundTag();
            if (wolf.save(data)) {
                CompoundTag tag = held.getOrCreateTag();
                tag.put(WOLF, data);
                tag.putString(NAME, wolf.getName().getString());
                level.playSound(null, wolf.getX(), wolf.getY(), wolf.getZ(),
                        SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1.0F, 0.8F);
                say(player, "stored", wolf.getName());
                wolf.discard();
            }
        }
        return InteractionResult.CONSUME;
    }

    // ---------- letting it out ----------

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        if (!hasWolf(stack)) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        CompoundTag data = stack.getTag().getCompound(WOLF).copy();
        OldIdMigration.fixEntityId(data); // DireWolves packed by versions before 1.22

        Optional<Entity> made = EntityType.create(data, server);
        if (made.isEmpty() || !(made.get() instanceof WolfHorseEntity wolf)) {
            return InteractionResult.FAIL;
        }
        float yaw = player != null ? player.getYRot() + 180.0F : 0.0F; // facing you
        wolf.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, yaw, 0.0F);
        wolf.setYHeadRot(yaw);
        wolf.setYBodyRot(yaw);
        if (!server.noCollision(wolf)) {
            if (player != null) {
                say(player, "no_room");
            }
            return InteractionResult.FAIL;
        }
        if (!server.addFreshEntity(wolf)) {
            // Only happens if the same DireWolf is already in the world (e.g. a creative copy of the backpack).
            wolf.setUUID(UUID.randomUUID());
            server.addFreshEntity(wolf);
        }
        stack.getTag().remove(WOLF);
        stack.getTag().remove(NAME);
        server.playSound(null, wolf.getX(), wolf.getY(), wolf.getZ(),
                SoundEvents.ARMOR_EQUIP_LEATHER, SoundSource.PLAYERS, 1.0F, 1.1F);
        if (player != null) {
            say(player, "released", wolf.getName());
        }
        return InteractionResult.CONSUME;
    }

    // ---------- keeping the DireWolf safe ----------

    /** Fireproof unless the config says it can burn. */
    @Override
    public boolean isFireResistant() {
        return !WolfHorseConfig.backpackCanBurn();
    }

    /** A full backpack dropped on the ground never despawns. */
    @Override
    public int getEntityLifespan(ItemStack stack, Level level) {
        return hasWolf(stack) ? Integer.MAX_VALUE : super.getEntityLifespan(stack, level);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    // ---------- tooltip ----------

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        if (hasWolf(stack)) {
            tooltip.add(Component.translatable("item.fotm.direwolf_backpack.holding",
                    Component.literal(stack.getTag().getString(NAME)).withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.fotm.direwolf_backpack.help.release")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            tooltip.add(Component.translatable("item.fotm.direwolf_backpack.empty").withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("item.fotm.direwolf_backpack.help.store")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
