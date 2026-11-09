package com.example.wolfhorse.client;

import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.entity.WolfHorseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Glowing red eyes on every DireWolf that came with a Blood Hunt. */
public class BloodEyesLayer extends RenderLayer<WolfHorseEntity, DireWolfModel> {
    private static final RenderType EYES = RenderType.eyes(
            new ResourceLocation(WolfHorseMod.MODID, "textures/entity/direwolf_blood_eyes.png"));

    public BloodEyesLayer(RenderLayerParent<WolfHorseEntity, DireWolfModel> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, WolfHorseEntity wolf,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!wolf.hasBloodEyes() || wolf.isInvisible()) {
            return;
        }
        DireWolfModel model = this.getParentModel();
        VertexConsumer vc = buffers.getBuffer(EYES);
        model.useTint = false;
        model.renderToBuffer(pose, vc, 0xF000F0, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        model.useTint = true;
    }
}
