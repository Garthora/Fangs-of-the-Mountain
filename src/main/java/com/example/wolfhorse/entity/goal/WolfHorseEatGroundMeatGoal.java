package com.example.wolfhorse.entity.goal;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;

/**
 * Walks over to meat lying on the ground nearby and eats one piece.
 * Eating heals it, and makes a Pup grow up faster. At most one piece every 10 seconds.
 */
public class WolfHorseEatGroundMeatGoal extends Goal {
    private static final double SEARCH_RADIUS = 10.0D;
    private static final int COOLDOWN_TICKS = 200;  // 10 seconds between bites
    private static final int GIVE_UP_TICKS = 200;   // stop chasing a piece it can't reach

    private final WolfHorseEntity mob;
    @Nullable
    private ItemEntity target;
    private int nextEatTick;
    private int ticksChasing;

    public WolfHorseEatGroundMeatGoal(WolfHorseEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    private boolean free() {
        return !this.mob.isOrderedToSit() && !this.mob.isVehicle() && this.mob.getTarget() == null;
    }

    @Override
    public boolean canUse() {
        if (this.mob.tickCount < this.nextEatTick || !free() || this.mob.getRandom().nextInt(10) != 0) {
            return false;
        }
        this.target = this.mob.level().getEntitiesOfClass(ItemEntity.class,
                        this.mob.getBoundingBox().inflate(SEARCH_RADIUS, 4.0D, SEARCH_RADIUS),
                        item -> item.isAlive() && WolfHorseEntity.isMeat(item.getItem(), this.mob)
                                && this.mob.isWithinRestriction(item.blockPosition()))
                .stream()
                .min(Comparator.comparingDouble(this.mob::distanceToSqr))
                .orElse(null);
        return this.target != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.target != null && this.target.isAlive() && free() && this.ticksChasing < GIVE_UP_TICKS;
    }

    @Override
    public void start() {
        this.ticksChasing = 0;
        this.mob.getNavigation().moveTo(this.target, 1.2D);
    }

    @Override
    public void stop() {
        this.target = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.target == null) {
            return;
        }
        this.ticksChasing++;
        this.mob.getLookControl().setLookAt(this.target, 30.0F, 30.0F);
        if (this.mob.getBoundingBox().inflate(1.0D).intersects(this.target.getBoundingBox())) {
            this.eat(this.target);
            this.target = null;
            this.nextEatTick = this.mob.tickCount + COOLDOWN_TICKS;
        } else if (this.ticksChasing % 10 == 0) {
            this.mob.getNavigation().moveTo(this.target, 1.2D);
        }
    }

    private void eat(ItemEntity item) {
        ItemStack stack = item.getItem().copy();
        ItemStack bite = stack.split(1);
        if (stack.isEmpty()) {
            item.discard();
        } else {
            item.setItem(stack);
        }
        this.mob.eatMeat(bite);
        if (this.mob.level() instanceof ServerLevel server) {
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, bite),
                    item.getX(), item.getY() + 0.2D, item.getZ(), 8, 0.2D, 0.1D, 0.2D, 0.05D);
        }
    }
}
