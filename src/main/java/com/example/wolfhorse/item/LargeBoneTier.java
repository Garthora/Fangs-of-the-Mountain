package com.example.wolfhorse.item;

import com.example.wolfhorse.WolfHorseMod;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

/** Same numbers as vanilla iron tools, but repaired with Large Bones. */
public enum LargeBoneTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 250; // iron
    }

    @Override
    public float getSpeed() {
        return 6.0F; // iron
    }

    @Override
    public float getAttackDamageBonus() {
        return 2.0F; // iron
    }

    @Override
    public int getLevel() {
        return 2; // iron
    }

    @Override
    public int getEnchantmentValue() {
        return 14; // iron
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(WolfHorseMod.DIREWOLF_BONE.get());
    }
}
