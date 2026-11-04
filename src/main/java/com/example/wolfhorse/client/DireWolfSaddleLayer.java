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
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/** A leather saddle drawn on the DireWolf's back when it's saddled. */
public class DireWolfSaddleLayer extends RenderLayer<WolfHorseEntity, DireWolfModel> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(WolfHorseMod.MODID, "direwolf"), "saddle");
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(WolfHorseMod.MODID, "textures/entity/direwolf_saddle.png");

    private final ModelPart saddle;

    public DireWolfSaddleLayer(RenderLayerParent<WolfHorseEntity, DireWolfModel> parent, EntityModelSet models) {
        super(parent);
        this.saddle = models.bakeLayer(LAYER).getChild("saddle");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        // Same coordinate space as the wolf body part; the top of the back is the +Z face.
        mesh.getRoot().addOrReplaceChild("saddle",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3.5F, 0.5F, -1.0F, 7.0F, 4.0F, 4.5F, new CubeDeformation(0.15F)),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 16);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, WolfHorseEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (!entity.isSaddled() || entity.isBaby()) {
            return;
        }
        this.saddle.copyFrom(this.getParentModel().body);
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.saddle.render(pose, vc, light, OverlayTexture.NO_OVERLAY);
    }
}
