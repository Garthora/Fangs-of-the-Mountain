package com.example.wolfhorse.entity.goal;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

/** Attacks whatever its owner just attacked. */
public class WolfHorseOwnerHurtTargetGoal extends TargetGoal {
    private final WolfHorseEntity mob;
    private LivingEntity victim;
    private int lastTimestamp;

    public WolfHorseOwnerHurtTargetGoal(WolfHorseEntity mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isTamed() || this.mob.isOrderedToSit() || this.mob.isVehicle()) {
            return false;
        }
        LivingEntity owner = this.mob.getOwnerEntity();
        if (owner == null) {
            return false;
        }
        this.victim = owner.getLastHurtMob();
        int ts = owner.getLastHurtMobTimestamp();
        return ts != this.lastTimestamp
                && this.victim != null
                && this.victim != this.mob
                && this.canAttack(this.victim, TargetingConditions.DEFAULT)
                && this.mob.wantsToAttack(this.victim, owner);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.victim);
        LivingEntity owner = this.mob.getOwnerEntity();
        if (owner != null) {
            this.lastTimestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
