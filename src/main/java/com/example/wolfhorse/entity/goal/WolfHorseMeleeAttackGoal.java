package com.example.wolfhorse.entity.goal;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/** Normal melee attack, but never while sitting or carrying a rider. One goal for tamed, one for wild. */
public class WolfHorseMeleeAttackGoal extends MeleeAttackGoal {
    private final WolfHorseEntity mob;
    private final boolean forTamed;

    public WolfHorseMeleeAttackGoal(WolfHorseEntity mob, double speed, boolean followEvenIfNotSeen, boolean forTamed) {
        super(mob, speed, followEvenIfNotSeen);
        this.mob = mob;
        this.forTamed = forTamed;
    }

    private boolean free() {
        return this.mob.isTamed() == this.forTamed && !this.mob.isOrderedToSit() && !this.mob.isVehicle();
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
