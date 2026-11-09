package com.example.wolfhorse.blood;

import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.entity.WolfHorseEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;

import java.util.Locale;

/**
 * The three kinds of Blood Hunt, one per DireWolf type.
 *  TUNDRA      - attacks everything in the village (villagers, golems, animals, players)
 *  PLAINS      - goes after livestock
 *  DARK_FOREST - eats the villagers
 */
public enum BloodHuntType {
    TUNDRA, PLAINS, DARK_FOREST;

    public final String id = this.name().toLowerCase(Locale.ROOT);

    public EntityType<WolfHorseEntity> entityType() {
        return switch (this) {
            case TUNDRA -> WolfHorseMod.TUNDRA_DIREWOLF.get();
            case PLAINS -> WolfHorseMod.PLAINS_DIREWOLF.get();
            case DARK_FOREST -> WolfHorseMod.DARK_FOREST_DIREWOLF.get();
        };
    }

    public Component displayName() {
        return Component.translatable("bloodhunt.fotm.type." + this.id);
    }

    public ResourceLocation rewardLootTable() {
        return new ResourceLocation(WolfHorseMod.MODID, "gameplay/blood_hunt_reward_" + this.id);
    }

    /** Which pack comes depends on where the hunt happens. */
    public static BloodHuntType forBiome(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.value().coldEnoughToSnow(pos)) {
            return TUNDRA;
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA) || biome.is(BiomeTags.IS_JUNGLE)) {
            return DARK_FOREST;
        }
        return PLAINS;
    }

    public static BloodHuntType byId(String id) {
        for (BloodHuntType t : values()) {
            if (t.id.equals(id)) {
                return t;
            }
        }
        return null;
    }
}
