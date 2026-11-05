package com.example.wolfhorse.item;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Right-click: every tamed DireWolf you own (in loaded parts of this dimension) stands up
 * and comes to you. Far-away ones teleport next to you.
 */
public class WolfWhistleItem extends Item {
    private static final int COOLDOWN_TICKS = 60; // 3 seconds
    private static final double WALK_RANGE_SQR = 12.0D * 12.0D;

    public WolfWhistleItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.NOTE_BLOCK_FLUTE.value(), SoundSource.PLAYERS, 1.0F, 2.0F);
        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);

        if (level instanceof ServerLevel server) {
            int called = 0;
            for (Entity e : server.getAllEntities()) {
                if (e instanceof WolfHorseEntity wolf && wolf.isAlive() && wolf.isTamed()
                        && wolf.isOwner(player) && !wolf.isVehicle() && !wolf.isLeashed()) {
                    wolf.setOrderedToSit(false);
                    wolf.setTarget(null);
                    boolean near = wolf.distanceToSqr(player) <= WALK_RANGE_SQR;
                    if (near || !wolf.teleportNear(player)) {
                        wolf.getNavigation().moveTo(player, 1.4D);
                    }
                    called++;
                }
            }
            player.displayClientMessage(called == 0
                    ? Component.translatable("message.wolfhorse.whistle.none")
                    : Component.translatable("message.wolfhorse.whistle.called", called), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
