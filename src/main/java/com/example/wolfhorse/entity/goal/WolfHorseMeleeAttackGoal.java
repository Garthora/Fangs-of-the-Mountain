package com.example.wolfhorse.entity.goal;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/** Normal melee attack, but never while sitting or carrying a rider. */
public class WolfHorseMeleeAttackGoal extends MeleeAttackGoal {
    private final WolfHorseEntity mob;

    public WolfHorseMeleeAttackGoal(WolfHorseEntity mob, double speed, boolean followEvenIfNotSeen) {
        super(mob, speed, followEvenIfNotSeen);
        this.mob = mob;
    }

    private boolean free() {
        return !this.mob.isOrderedToSit() && !this.mob.isVehicle();
    }

    @Override
    public boolean canUse() {
        return free() && super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return free() && super.canContinueToUse();
    }
}
