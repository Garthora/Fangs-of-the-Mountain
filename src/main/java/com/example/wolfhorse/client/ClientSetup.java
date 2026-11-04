package com.example.wolfhorse.client;

import com.example.wolfhorse.WolfHorseMod;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WolfHorseMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // The Wolf Horse is a Horse subclass, so the vanilla horse renderer
        // (coat colours, markings, saddle and armor) works as-is.
        event.registerEntityRenderer(WolfHorseMod.WOLF_HORSE.get(), HorseRenderer::new);
    }
}
