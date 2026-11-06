package com.example.wolfhorse.item;

import com.example.wolfhorse.WolfHorseMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.armortrim.ArmorTrim;

/** The three fur colours, one per DireWolf type. Also used as armor-trim materials. */
public enum FurVariant {
    PLAINS("plains"),  // tan to dark grey
    FOREST("forest"),  // brown to black
    TUNDRA("tundra");  // white to grey

    public final String id;
    /** The trim material made from this fur, e.g. wolfhorse:forest_fur. */
    public final ResourceLocation trimMaterial;

    FurVariant(String id) {
        this.id = id;
        this.trimMaterial = new ResourceLocation(WolfHorseMod.MODID, id + "_fur");
    }

    /** Fur colour for a pelt or scarf trim: the fur used as the trim material, or Plains for anything else. */
    public static FurVariant fromTrim(ArmorTrim trim) {
        return trim.material().unwrapKey().map(key -> {
            for (FurVariant v : values()) {
                if (v.trimMaterial.equals(key.location())) {
                    return v;
                }
            }
            return PLAINS;
        }).orElse(PLAINS);
    }
}
