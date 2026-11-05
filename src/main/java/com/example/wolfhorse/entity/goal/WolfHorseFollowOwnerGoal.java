package com.example.wolfhorse.entity.goal;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumSet;

/** Wolf-style follow: walks after the owner and teleports if it falls far behind. */
public class WolfHorseFollowOwnerGoal extends Goal {
    private static final double TELEPORT_DIST_SQR = 16.0D * 16.0D;

    private final WolfHorseEntity mob;
    private final double speed;
    private final float startDistance;
    private final float stopDistance;
    private LivingEntity owner;
    private int recalcTimer;

    public WolfHorseFollowOwnerGoal(WolfHorseEntity mob, double speed, float startDistance, float stopDistance) {
        this.mob = mob;
        this.speed = speed;
        this.startDistance = startDistance;
        this.stopDistance = stopDistance;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isTamed() || this.mob.isOrderedToSit() || this.mob.isGuarding() || this.mob.isVehicle() || this.mob.isLeashed()) {
            return false;
        }
        LivingEntity o = this.mob.getOwnerEntity();
        if (o == null || o.isSpectator()) {
            return false;
        }
        if (this.mob.distanceToSqr(o) < this.startDistance * this.startDistance) {
            return false;
        }
        this.owner = o;
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        return this.owner != null
                && !this.mob.getNavigation().isDone()
                && !this.mob.isOrderedToSit()
                && !this.mob.isGuarding()
                && !this.mob.isVehicle()
                && this.mob.distanceToSqr(this.owner) > this.stopDistance * this.stopDistance;
    }

    @Override
    public void start() {
        this.recalcTimer = 0;
    }

    @Override
    public void stop() {
        this.owner = null;
        this.mob.getNavigation().stop();
    }

    @Override
    public void tick() {
        this.mob.getLookControl().setLookAt(this.owner, 10.0F, this.mob.getMaxHeadXRot());
        if (--this.recalcTimer > 0) {
            return;
        }
        this.recalcTimer = this.adjustedTickDelay(10);
        if (this.mob.distanceToSqr(this.owner) >= TELEPORT_DIST_SQR) {
            this.teleportToOwner();
        } else {
            this.mob.getNavigation().moveTo(this.owner, this.speed);
        }
    }

    private void teleportToOwner() {
        BlockPos center = this.owner.blockPosition();
        for (int i = 0; i < 10; i++) {
            int dx = this.mob.getRandom().nextIntBetweenInclusive(-3, 3);
            int dy = this.mob.getRandom().nextIntBetweenInclusive(-1, 1);
            int dz = this.mob.getRandom().nextIntBetweenInclusive(-3, 3);
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                continue; // don't land on top of the owner
            }
            BlockPos target = center.offset(dx, dy, dz);
            if (canTeleportTo(target)) {
                this.mob.moveTo(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D,
                        this.mob.getYRot(), this.mob.getXRot());
                this.mob.getNavigation().stop();
                return;
            }
        }
    }

    private boolean canTeleportTo(BlockPos pos) {
        BlockState below = this.mob.level().getBlockState(pos.below());
        if (below.isAir() || below.getBlock() instanceof LeavesBlock || !below.getFluidState().isEmpty()) {
            return false;
        }
        BlockPos offset = pos.subtract(this.mob.blockPosition());
        return this.mob.level().noCollision(this.mob, this.mob.getBoundingBox().move(offset));
    }
}
