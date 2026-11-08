package com.example.wolfhorse;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Settings file: config/fangs-of-the-mountains.toml (created the first time the game starts).
 * On a server, the server's copy is the one that counts.
 */
public final class WolfHorseConfig {
    public enum FurArmorMode {
        /** Craft full Fur armor sets (Helmet, Chestplate, Leggings, Boots). */
        SET,
        /** No Fur armor sets: put a fur coat on any armor with the Fur Armor Trim smithing template. */
        TRIM,
        /** Both of the above. */
        BOTH
    }

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue BACKPACK_CAN_BURN;
    private static final ForgeConfigSpec.EnumValue<FurArmorMode> FUR_ARMOR_MODE;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("backpack");
        BACKPACK_CAN_BURN = b
                .comment("Can the DireWolf Backpack burn up in fire or lava, like a normal item?",
                        "false = it's fireproof (default). true = it burns, and a DireWolf inside is lost.")
                .define("backpackCanBurn", false);
        b.pop();

        b.push("fur_armor");
        FUR_ARMOR_MODE = b
                .comment("How players get fur armor:",
                        "  SET  = craft full Fur armor sets (default)",
                        "  TRIM = no Fur armor sets; use the Fur Armor Trim smithing template to put a fur coat on any armor",
                        "  BOTH = both ways",
                        "Restart the game (or use /reload in a world) after changing this.",
                        "Fur armor that already exists in a world keeps working either way.")
                .defineEnum("furArmorMode", FurArmorMode.SET);
        b.pop();

        SPEC = b.build();
    }

    private WolfHorseConfig() {
    }

    public static boolean backpackCanBurn() {
        return SPEC.isLoaded() ? BACKPACK_CAN_BURN.get() : false;
    }

    public static FurArmorMode furArmorMode() {
        return SPEC.isLoaded() ? FUR_ARMOR_MODE.get() : FurArmorMode.SET;
    }

    public static boolean furArmorSetsEnabled() {
        return furArmorMode() != FurArmorMode.TRIM;
    }

    public static boolean furArmorTrimEnabled() {
        return furArmorMode() != FurArmorMode.SET;
    }
}
