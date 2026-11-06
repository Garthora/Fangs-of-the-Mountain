package com.example.wolfhorse.item;

import com.example.wolfhorse.WolfHorseMod;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/** Fur armor: same numbers as vanilla iron armor, repaired with its own fur. One per biome variant. */
public enum FurArmorMaterial implements ArmorMaterial {
    // "fur" keeps the original texture/material name so Plains armor in existing worlds is unchanged.
    PLAINS("fur", () -> WolfHorseMod.FUR.get()),
    FOREST("forest_fur", () -> WolfHorseMod.FOREST_FUR.get()),
    TUNDRA("tundra_fur", () -> WolfHorseMod.TUNDRA_FUR.get());

    private static final int DURABILITY_MULTIPLIER = 15; // iron

    private final String name;
    private final Supplier<Item> repairItem;

    FurArmorMaterial(String name, Supplier<Item> repairItem) {
        this.name = name;
        this.repairItem = repairItem;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        int base = switch (type) {
            case HELMET -> 11;
            case CHESTPLATE -> 16;
            case LEGGINGS -> 15;
            case BOOTS -> 13;
        };
        return base * DURABILITY_MULTIPLIER;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return switch (type) {
            case HELMET -> 2;
            case CHESTPLATE -> 6;
            case LEGGINGS -> 5;
            case BOOTS -> 2;
        };
    }

    @Override
    public int getEnchantmentValue() {
        return 9; // iron
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(this.repairItem.get());
    }

    @Override
    public String getName() {
        // textures: assets/wolfhorse/textures/models/armor/<name>_layer_1.png and _layer_2.png
        return WolfHorseMod.MODID + ":" + this.name;
    }

    @Override
    public float getToughness() {
        return 0.0F;
    }

    @Override
    public float getKnockbackResistance() {
        return 0.0F;
    }
}
