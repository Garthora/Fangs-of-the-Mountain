package com.example.wolfhorse.client;

import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.entity.WolfHorseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** Draws the Tundra DireWolf with the custom Blockbench model. */
public class TundraDireWolfRenderer extends MobRenderer<WolfHorseEntity, TundraDireWolfModel> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(WolfHorseMod.MODID, "textures/entity/tundra_direwolf.png");

    public TundraDireWolfRenderer(EntityRendererProvider.Context context) {
        super(context, new TundraDireWolfModel(context.bakeLayer(TundraDireWolfModel.LAYER)), 1.0F);
        this.addLayer(new Saddle(this, context.getModelSet()));
        this.addLayer(new Eyes(this));
    }

    @Override
    protected void scale(WolfHorseEntity entity, PoseStack pose, float partialTick) {
        if (entity.isBaby()) {
            pose.scale(0.5F, 0.5F, 0.5F); // Pups
        }
    }

    @Override
    public ResourceLocation getTextureLocation(WolfHorseEntity entity) {
        return TEXTURE;
    }

    /** Glowing red eyes for Blood Hunt wolves (the eye strips of the texture). */
    static class Eyes extends RenderLayer<WolfHorseEntity, TundraDireWolfModel> {
        private static final RenderType EYES = RenderType.eyes(
                new ResourceLocation(WolfHorseMod.MODID, "textures/entity/tundra_direwolf_blood_eyes.png"));

        Eyes(RenderLayerParent<WolfHorseEntity, TundraDireWolfModel> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, WolfHorseEntity wolf,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            if (!wolf.hasBloodEyes() || wolf.isInvisible()) {
                return;
            }
            TundraDireWolfModel model = this.getParentModel();
            model.useTint = false;
            model.renderToBuffer(pose, buffers.getBuffer(EYES), 0xF000F0, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            model.useTint = true;
        }
    }

    /** A leather saddle on the back, behind the shoulders. */
    static class Saddle extends RenderLayer<WolfHorseEntity, TundraDireWolfModel> {
        static final ModelLayerLocation LAYER =
                new ModelLayerLocation(new ResourceLocation(WolfHorseMod.MODID, "tundra_direwolf"), "saddle");
        private static final ResourceLocation TEXTURE =
                new ResourceLocation(WolfHorseMod.MODID, "textures/entity/tundra_direwolf_saddle.png");
        private final ModelPart saddle;

        Saddle(RenderLayerParent<WolfHorseEntity, TundraDireWolfModel> parent, EntityModelSet models) {
            super(parent);
            this.saddle = models.bakeLayer(LAYER).getChild("saddle");
        }

        /** In the body's space: the top of the back is at y = -7. */
        static LayerDefinition createLayer() {
            MeshDefinition mesh = new MeshDefinition();
            mesh.getRoot().addOrReplaceChild("saddle", CubeListBuilder.create().texOffs(0, 0)
                            .addBox(-6.5F, -8.5F, -10.0F, 13.0F, 3.0F, 10.0F, new CubeDeformation(0.2F)),
                    PartPose.ZERO);
            return LayerDefinition.create(mesh, 64, 32);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buffers, int light, WolfHorseEntity wolf,
                           float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                           float netHeadYaw, float headPitch) {
            if (!wolf.isSaddled() || wolf.isBaby()) {
                return;
            }
            pose.pushPose();
            TundraDireWolfModel model = this.getParentModel();
            model.root().translateAndRotate(pose);
            model.body.translateAndRotate(pose);
            VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
            this.saddle.render(pose, vc, light, OverlayTexture.NO_OVERLAY);
            pose.popPose();
        }
    }
}
