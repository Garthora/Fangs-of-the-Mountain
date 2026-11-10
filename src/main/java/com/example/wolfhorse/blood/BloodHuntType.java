package com.example.wolfhorse.blood;

import com.example.wolfhorse.WolfHorseConfig;
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
 *  RAVENOUS - Hunt of the Ravenous (Tundra DireWolves): attacks everything in the village
 *  HUNGRY   - Hunt of the Hungry (Plains DireWolves): goes after livestock
 *  HATEFUL  - Hunt of the Hateful (Dark Forest DireWolves): eats the villagers
 */
public enum BloodHuntType {
    RAVENOUS, HUNGRY, HATEFUL;

    public final String id = this.name().toLowerCase(Locale.ROOT);

    public EntityType<WolfHorseEntity> entityType() {
        return switch (this) {
            case RAVENOUS -> WolfHorseMod.TUNDRA_DIREWOLF.get();
            case HUNGRY -> WolfHorseMod.PLAINS_DIREWOLF.get();
            case HATEFUL -> WolfHorseMod.DARK_FOREST_DIREWOLF.get();
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
            return RAVENOUS;
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_TAIGA) || biome.is(BiomeTags.IS_JUNGLE)) {
            return HATEFUL;
        }
        return HUNGRY;
    }

    public boolean enabled() {
        return WolfHorseConfig.huntEnabled(this);
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
