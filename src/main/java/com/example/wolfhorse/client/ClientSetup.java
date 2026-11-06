package com.example.wolfhorse.client;

import com.example.wolfhorse.WolfHorseMod;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = WolfHorseMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(DireWolfSaddleLayer.LAYER, DireWolfSaddleLayer::createLayer);
        event.registerLayerDefinition(ScarfLayer.LAYER, ScarfLayer::createLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (var type : WolfHorseMod.allDirewolves()) {
            event.registerEntityRenderer(type.get(), DireWolfRenderer::new);
        }
    }

    /** Show the Pelt and Scarf on players (both skin types) and armor stands. */
    @SubscribeEvent
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new PeltLayer<>(renderer, event.getEntityModels()));
                renderer.addLayer(new ScarfLayer<>(renderer, event.getEntityModels()));
            }
        }
        ArmorStandRenderer stand = event.getRenderer(EntityType.ARMOR_STAND);
        if (stand != null) {
            stand.addLayer(new PeltLayer<>(stand, event.getEntityModels()));
            stand.addLayer(new ScarfLayer<>(stand, event.getEntityModels()));
        }
    }
}
