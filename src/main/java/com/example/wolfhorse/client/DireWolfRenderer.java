package com.example.wolfhorse.client;

import com.example.wolfhorse.entity.WolfHorseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Renders the DireWolf as a horse-sized wolf. */
public class DireWolfRenderer extends MobRenderer<WolfHorseEntity, DireWolfModel> {
    public static final float SCALE = 1.8F;

    private static final ResourceLocation WOLF = new ResourceLocation("textures/entity/wolf/wolf.png");
    private static final ResourceLocation WOLF_TAME = new ResourceLocation("textures/entity/wolf/wolf_tame.png");

    public DireWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new DireWolfModel(context.bakeLayer(ModelLayers.WOLF)), 1.0F);
        this.addLayer(new DireWolfSaddleLayer(this, context.getModelSet()));
    }

    @Override
    protected void scale(WolfHorseEntity entity, PoseStack pose, float partialTick) {
        pose.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(WolfHorseEntity entity) {
        return entity.isTamed() ? WOLF_TAME : WOLF;
    }
}
