package com.example.wolfhorse.entity.goal;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

/** Holds the horse in place while it's been told to sit. */
public class WolfHorseSitGoal extends Goal {
    private final WolfHorseEntity mob;

    public WolfHorseSitGoal(WolfHorseEntity mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.mob.isTamed()
                && this.mob.isOrderedToSit()
                && !this.mob.isVehicle()
                && !this.mob.isInWaterOrBubble()
                && this.mob.onGround();
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.isOrderedToSit() && !this.mob.isVehicle();
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.setTarget(null);
    }
}
