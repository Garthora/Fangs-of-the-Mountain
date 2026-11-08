package com.example.wolfhorse;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.MissingMappingsEvent;

/**
 * Before 1.22 the mod's id was "wolfhorse". Worlds saved with an older version are converted
 * when they load: every wolfhorse:* item and entity becomes the matching fotm:* one.
 * (Trims and den chests from old worlds are covered by the files in data/wolfhorse/.)
 */
@Mod.EventBusSubscriber(modid = WolfHorseMod.MODID)
public final class OldIdMigration {
    public static final String OLD_ID = "wolfhorse";

    private OldIdMigration() {
    }

    /** The few things that were also renamed, not just moved to the new id. */
    public static String newPath(String oldPath) {
        return switch (oldPath) {
            case "wolf_horse" -> "plains_direwolf";
            case "wolf_horse_spawn_egg" -> "plains_direwolf_spawn_egg";
            default -> oldPath;
        };
    }

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        remap(event, Registries.ITEM, ForgeRegistries.ITEMS);
        remap(event, Registries.ENTITY_TYPE, ForgeRegistries.ENTITY_TYPES);
    }

    private static <T> void remap(MissingMappingsEvent event, ResourceKey<? extends Registry<T>> key,
                                  IForgeRegistry<T> registry) {
        for (MissingMappingsEvent.Mapping<T> mapping : event.getMappings(key, OLD_ID)) {
            ResourceLocation target = new ResourceLocation(WolfHorseMod.MODID, newPath(mapping.getKey().getPath()));
            if (registry.containsKey(target)) {
                mapping.remap(registry.getValue(target));
            }
        }
    }

    /** For a DireWolf saved inside an item (the backpack) by an older version. */
    public static void fixEntityId(CompoundTag entity) {
        String id = entity.getString("id");
        if (id.startsWith(OLD_ID + ":")) {
            entity.putString("id", WolfHorseMod.MODID + ":" + newPath(id.substring(OLD_ID.length() + 1)));
        }
    }
}
