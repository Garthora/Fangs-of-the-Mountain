package com.example.wolfhorse;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;

/**
 * Recipe condition: {"type": "wolfhorse:fur_armor", "allows": "sets"} or {"allows": "trim"}.
 * Turns Fur armor recipes and the Fur Armor Trim recipes on or off from the config.
 */
public record FurArmorModeCondition(String allows) implements ICondition {
    public static final ResourceLocation ID = new ResourceLocation(WolfHorseMod.MODID, "fur_armor");

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(IContext context) {
        return "trim".equals(this.allows)
                ? WolfHorseConfig.furArmorTrimEnabled()
                : WolfHorseConfig.furArmorSetsEnabled();
    }

    public static final IConditionSerializer<FurArmorModeCondition> SERIALIZER = new IConditionSerializer<>() {
        @Override
        public void write(JsonObject json, FurArmorModeCondition value) {
            json.addProperty("allows", value.allows());
        }

        @Override
        public FurArmorModeCondition read(JsonObject json) {
            return new FurArmorModeCondition(GsonHelper.getAsString(json, "allows"));
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    };
}
