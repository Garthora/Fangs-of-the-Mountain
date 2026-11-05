package com.example.wolfhorse.item;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Tap right-click: use the selected command.
 * Hold right-click: cycle through the commands (one step per second), let go to keep it.
 */
public class WolfWhistleItem extends Item {
    public enum Mode {
        CALL("call"),     // all your loaded DireWolves come to you, far ones teleport
        SIT("sit"),       // nearby ones sit
        FOLLOW("follow"); // nearby ones stand up and follow

        final String key;

        Mode(String key) {
            this.key = key;
        }

        Component label() {
            return Component.translatable("item.wolfhorse.wolf_whistle.mode." + this.key);
        }

        Mode next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    private static final int TAP_MAX_TICKS = 10;     // shorter than half a second = tap
    private static final int CYCLE_EVERY_TICKS = 20; // then one mode step per second
    private static final double WALK_RANGE_SQR = 12.0D * 12.0D;
    private static final double COMMAND_RANGE_SQR = 32.0D * 32.0D;

    public WolfWhistleItem(Properties properties) {
        super(properties);
    }

    public static Mode getMode(ItemStack stack) {
        int i = stack.hasTag() ? stack.getTag().getInt("Mode") : 0;
        Mode[] all = Mode.values();
        return all[Math.floorMod(i, all.length)];
    }

    private static void setMode(ItemStack stack, Mode mode) {
        stack.getOrCreateTag().putInt("Mode", mode.ordinal());
    }

    // ---------- holding vs tapping ----------

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
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (level.isClientSide || !(user instanceof Player player)) {
            return;
        }
        int held = this.getUseDuration(stack) - remaining;
        if (held >= TAP_MAX_TICKS && (held - TAP_MAX_TICKS) % CYCLE_EVERY_TICKS == 0) {
            Mode mode = getMode(stack).next();
            setMode(stack, mode);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.PLAYERS, 0.6F, 1.6F);
            player.displayClientMessage(Component.translatable("message.wolfhorse.whistle.mode",
                    mode.label().copy().withStyle(ChatFormatting.GOLD)), true);
        }
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
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.0F,
                mode == Mode.SIT ? 1.4F : 2.0F);
        player.getCooldowns().addCooldown(this, mode == Mode.CALL ? 60 : 20);

        if (!(level instanceof ServerLevel server)) {
            return;
        }
        int count = 0;
        for (Entity e : server.getAllEntities()) {
            if (!(e instanceof WolfHorseEntity wolf) || !wolf.isAlive() || !wolf.isTamed()
                    || !wolf.isOwner(player) || wolf.isVehicle()) {
                continue;
            }
            double distSqr = wolf.distanceToSqr(player);
            switch (mode) {
                case CALL -> {
                    if (wolf.isLeashed()) {
                        continue;
                    }
                    wolf.setOrderedToSit(false);
                    wolf.setTarget(null);
                    if (distSqr <= WALK_RANGE_SQR || !wolf.teleportNear(player)) {
                        wolf.getNavigation().moveTo(player, 1.4D);
                    }
                }
                case SIT -> {
                    if (distSqr > COMMAND_RANGE_SQR) {
                        continue;
                    }
                    wolf.setOrderedToSit(true);
                    wolf.setTarget(null);
                    wolf.getNavigation().stop();
                }
                case FOLLOW -> {
                    if (distSqr > COMMAND_RANGE_SQR) {
                        continue;
                    }
                    wolf.setOrderedToSit(false);
                }
            }
            count++;
        }
        player.displayClientMessage(count == 0
                ? Component.translatable("message.wolfhorse.whistle.none")
                : Component.translatable("message.wolfhorse.whistle." + mode.key, count), true);
    }

    // ---------- tooltip ----------

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("message.wolfhorse.whistle.mode",
                getMode(stack).label().copy().withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.wolfhorse.wolf_whistle.help.tap").withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("item.wolfhorse.wolf_whistle.help.hold").withStyle(ChatFormatting.DARK_GRAY));
    }
}
