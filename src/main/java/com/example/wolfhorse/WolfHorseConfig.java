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
    private static final ForgeConfigSpec.BooleanValue FULL_MOON_HUNTS;
    private static final ForgeConfigSpec.DoubleValue FULL_MOON_CHANCE;
    private static final ForgeConfigSpec.BooleanValue RAVENOUS, HUNGRY, HATEFUL;

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

        b.push("blood_hunts");
        FULL_MOON_HUNTS = b
                .comment("Can Blood Hunts start on their own on full-moon nights?",
                        "(The Blood Hunt Horn and the /bloodhunt command work either way.)")
                .define("fullMoonBloodHunts", true);
        FULL_MOON_CHANCE = b
                .comment("On each full-moon night, the chance (0.0 to 1.0) that a Blood Hunt comes for a player",
                        "who is near a village or farm animals. 1.0 = every full moon.")
                .defineInRange("fullMoonChance", 1.0D, 0.0D, 1.0D);
        RAVENOUS = b
                .comment("Hunt of the Ravenous: Tundra DireWolves attack everything in the village (snowy biomes).",
                        "false = it never happens: not on full moons, not from the Blood Hunt Horn, not by command.")
                .define("huntOfTheRavenous", true);
        HUNGRY = b
                .comment("Hunt of the Hungry: Plains DireWolves go after livestock (most other biomes).")
                .define("huntOfTheHungry", true);
        HATEFUL = b
                .comment("Hunt of the Hateful: Dark Forest DireWolves eat the villagers (forest, taiga and jungle biomes).")
                .define("huntOfTheHateful", true);
        b.pop();

        SPEC = b.build();
    }

    private WolfHorseConfig() {
    }

    public static boolean backpackCanBurn() {
        return SPEC.isLoaded() ? BACKPACK_CAN_BURN.get() : false;
    }

    public static boolean fullMoonBloodHunts() {
        return SPEC.isLoaded() ? FULL_MOON_HUNTS.get() : true;
    }

    /** Is this kind of Blood Hunt switched on in the config? */
    public static boolean huntEnabled(com.example.wolfhorse.blood.BloodHuntType type) {
        if (!SPEC.isLoaded()) {
            return true;
        }
        return switch (type) {
            case RAVENOUS -> RAVENOUS.get();
            case HUNGRY -> HUNGRY.get();
            case HATEFUL -> HATEFUL.get();
        };
    }

    public static double fullMoonBloodHuntChance() {
        return SPEC.isLoaded() ? FULL_MOON_CHANCE.get() : 1.0D;
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
