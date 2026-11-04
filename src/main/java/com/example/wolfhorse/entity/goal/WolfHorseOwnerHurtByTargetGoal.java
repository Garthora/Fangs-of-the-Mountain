package com.example.wolfhorse.entity.goal;

import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

/** Attacks whatever just hurt its owner. */
public class WolfHorseOwnerHurtByTargetGoal extends TargetGoal {
    private final WolfHorseEntity mob;
    private LivingEntity attacker;
    private int lastTimestamp;

    public WolfHorseOwnerHurtByTargetGoal(WolfHorseEntity mob) {
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
        this.attacker = owner.getLastHurtByMob();
        int ts = owner.getLastHurtByMobTimestamp();
        return ts != this.lastTimestamp
                && this.attacker != null
                && this.canAttack(this.attacker, TargetingConditions.DEFAULT)
                && this.mob.wantsToAttack(this.attacker, owner);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.attacker);
        LivingEntity owner = this.mob.getOwnerEntity();
        if (owner != null) {
            this.lastTimestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
