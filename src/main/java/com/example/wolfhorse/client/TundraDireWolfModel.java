package com.example.wolfhorse.client;

import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.entity.WolfHorseEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * The Tundra DireWolf, from the Blockbench model blockbench/tundra_direwolf.bbmodel
 * (converted from GeckoLib to Minecraft's built-in model format, so no extra library is needed).
 * Already horse-sized: drawn at 1x. Texture: 128x128.
 */
public class TundraDireWolfModel extends HierarchicalModel<WolfHorseEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(WolfHorseMod.MODID, "tundra_direwolf"), "main");

    private final ModelPart root;
    final ModelPart body;
    private final ModelPart head;
    private final ModelPart back;
    private final ModelPart tail;
    private final ModelPart frontRight;
    private final ModelPart frontLeft;
    private final ModelPart backRight;
    private final ModelPart backLeft;

    private float tintR = 1.0F, tintG = 1.0F, tintB = 1.0F;
    /** Turned off while drawing the glowing eyes. */
    boolean useTint = true;

    public TundraDireWolfModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("direwolf");
        this.head = this.body.getChild("head");
        this.back = this.body.getChild("back");
        this.tail = this.back.getChild("tail");
        this.frontRight = this.body.getChild("front_right_leg_1");
        this.frontLeft = this.body.getChild("front_left_leg_1");
        this.backRight = this.back.getChild("back_right_leg_1");
        this.backLeft = this.back.getChild("back_left_leg_1");
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

    @Override
    public void prepareMobModel(WolfHorseEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        float[] tint = DireWolfModel.tintFor(entity.getBreed(), entity.getVariant());
        this.tintR = tint[0];
        this.tintG = tint[1];
        this.tintB = tint[2];
    }

    @Override
    public void setupAnim(WolfHorseEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose); // back to the pose from Blockbench

        boolean sitting = entity.isOrderedToSit() && !entity.isVehicle();
        if (sitting) {
            // chest up, hips down, back legs folded forward under the body
            this.body.y += 2.0F;
            this.body.xRot -= 0.45F;
            this.frontRight.xRot += 0.45F;
            this.frontLeft.xRot += 0.45F;
            this.backRight.xRot -= 1.1F;
            this.backLeft.xRot -= 1.1F;
        } else {
            // walk: diagonal legs move together, like a real wolf
            float a = Mth.cos(limbSwing * 0.6662F) * 0.9F * limbSwingAmount;
            float b = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 0.9F * limbSwingAmount;
            this.frontRight.xRot += a;
            this.backLeft.xRot += a;
            this.frontLeft.xRot += b;
            this.backRight.xRot += b;
        }
        this.head.yRot += netHeadYaw * Mth.DEG_TO_RAD;
        this.head.xRot += headPitch * Mth.DEG_TO_RAD;
        this.tail.yRot += Mth.cos(limbSwing * 0.6662F) * 0.5F * limbSwingAmount;
        if (entity.isTamed()) {
            this.tail.xRot -= 0.25F; // tamed ones carry their tail higher
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,
                               float r, float g, float b, float a) {
        if (this.useTint) {
            super.renderToBuffer(pose, buffer, light, overlay, r * this.tintR, g * this.tintG, b * this.tintB, a);
        } else {
            super.renderToBuffer(pose, buffer, light, overlay, r, g, b, a);
        }
    }

    // ---------- generated from the Blockbench model ----------
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition direwolf = root.addOrReplaceChild("direwolf", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6F, -7F, -14F, 12F, 13F, 16F)
                        .texOffs(52, 78).addBox(0F, -13F, -12F, 0F, 6F, 12F)
                        .texOffs(36, 58).addBox(-3F, -11F, -14F, 0F, 4F, 16F)
                        .texOffs(68, 58).addBox(3F, -11F, -14F, 0F, 4F, 16F),
                PartPose.offsetAndRotation(0F, 8F, 0F, Mth.DEG_TO_RAD * -5F, 0.0F, 0.0F));
        PartDefinition front_right_leg_1 = direwolf.addOrReplaceChild("front_right_leg_1", CubeListBuilder.create()
                        .texOffs(76, 78).addBox(-3F, 0F, -3F, 4F, 8F, 7F),
                PartPose.offsetAndRotation(-5F, -2F, -9F, Mth.DEG_TO_RAD * 15F, 0.0F, 0.0F));
        PartDefinition front_right_leg_2 = front_right_leg_1.addOrReplaceChild("front_right_leg_2", CubeListBuilder.create()
                        .texOffs(92, 45).addBox(-1F, -2F, -3F, 3F, 6F, 5F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(-1F, 8F, 1F, Mth.DEG_TO_RAD * 35F, 0.0F, 0.0F));
        PartDefinition front_right_leg_3 = front_right_leg_2.addOrReplaceChild("front_right_leg_3", CubeListBuilder.create()
                        .texOffs(36, 96).addBox(-1F, -2F, -2F, 3F, 8F, 4F)
                        .texOffs(90, 105).addBox(0.5F, -2F, 2F, 0F, 5F, 2F),
                PartPose.offsetAndRotation(0F, 4F, -1F, Mth.DEG_TO_RAD * -60F, 0.0F, 0.0F));
        PartDefinition front_right_foot = front_right_leg_3.addOrReplaceChild("front_right_foot", CubeListBuilder.create()
                        .texOffs(60, 0).addBox(-1.5F, -0.025F, -6F, 3F, 2F, 6.25F),
                PartPose.offsetAndRotation(0.5F, 5F, 2F, Mth.DEG_TO_RAD * 15F, 0.0F, 0.0F));
        front_right_foot.addOrReplaceChild("front_right_foot_r1", CubeListBuilder.create()
                        .texOffs(78, 1).addBox(-1.5F, -2F, -5.75F, 2.5F, 2F, 5.75F),
                PartPose.offsetAndRotation(0F, 2F, 0F, 0.0F, Mth.DEG_TO_RAD * 10F, 0.0F));
        front_right_foot.addOrReplaceChild("front_right_foot_r2", CubeListBuilder.create()
                        .texOffs(78, 1).addBox(-1F, -2F, -5.75F, 2.5F, 2F, 5.75F),
                PartPose.offsetAndRotation(0F, 2F, 0F, 0.0F, Mth.DEG_TO_RAD * -10F, 0.0F));
        PartDefinition front_left_leg_1 = direwolf.addOrReplaceChild("front_left_leg_1", CubeListBuilder.create()
                        .texOffs(0, 92).addBox(-1F, 0F, -3F, 4F, 8F, 7F),
                PartPose.offsetAndRotation(5F, -2F, -9F, Mth.DEG_TO_RAD * 15F, 0.0F, 0.0F));
        PartDefinition front_left_leg_2 = front_left_leg_1.addOrReplaceChild("front_left_leg_2", CubeListBuilder.create()
                        .texOffs(76, 93).addBox(-2F, -2F, -3F, 3F, 6F, 5F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(1F, 8F, 1F, Mth.DEG_TO_RAD * 35F, 0.0F, 0.0F));
        PartDefinition front_left_leg_3 = front_left_leg_2.addOrReplaceChild("front_left_leg_3", CubeListBuilder.create()
                        .texOffs(50, 96).addBox(-2F, -2F, -2F, 3F, 8F, 4F)
                        .texOffs(94, 105).addBox(-0.5F, -2F, 2F, 0F, 5F, 2F),
                PartPose.offsetAndRotation(0F, 4F, -1F, Mth.DEG_TO_RAD * -60F, 0.0F, 0.0F));
        PartDefinition front_left_foot = front_left_leg_3.addOrReplaceChild("front_left_foot", CubeListBuilder.create()
                        .texOffs(60, 0).addBox(-1.5F, -0.025F, -6F, 3F, 2F, 6.25F),
                PartPose.offsetAndRotation(-0.5F, 5F, 2F, Mth.DEG_TO_RAD * 15F, 0.0F, 0.0F));
        front_left_foot.addOrReplaceChild("front_left_foot_r3", CubeListBuilder.create()
                        .texOffs(78, 1).addBox(-1F, -2F, -5.75F, 2.5F, 2F, 5.75F),
                PartPose.offsetAndRotation(0F, 2F, 0F, 0.0F, Mth.DEG_TO_RAD * -10F, 0.0F));
        front_left_foot.addOrReplaceChild("front_left_foot_r4", CubeListBuilder.create()
                        .texOffs(78, 1).addBox(-1.5F, -2F, -5.75F, 2.5F, 2F, 5.75F),
                PartPose.offsetAndRotation(0F, 2F, 0F, 0.0F, Mth.DEG_TO_RAD * 10F, 0.0F));
        PartDefinition head = direwolf.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(56, 22).addBox(-5F, -5F, -9F, 10F, 10F, 8F),
                PartPose.offsetAndRotation(0F, -1F, -13F, 0.0F, 0.0F, 0.0F));
        head.addOrReplaceChild("head_r5", CubeListBuilder.create()
                        .texOffs(0, 3).addBox(0F, 0F, 0F, 3F, 2F, 0F),
                PartPose.offsetAndRotation(-4.5F, -2.5F, -9.1F, 0.0F, 0.0F, Mth.DEG_TO_RAD * 7.5F));
        head.addOrReplaceChild("head_r6", CubeListBuilder.create()
                        .texOffs(0, 1).mirror().addBox(-3F, 0F, 0F, 3F, 2F, 0F),
                PartPose.offsetAndRotation(4.5F, -2.5F, -9.1F, 0.0F, 0.0F, Mth.DEG_TO_RAD * -7.5F));
        PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create()
                        .texOffs(93, 16).addBox(-2F, -1F, -5F, 4F, 3F, 5F)
                        .texOffs(92, 58).addBox(-2.5F, 2F, -4.5F, 5F, 2F, 4F),
                PartPose.offsetAndRotation(0F, 1F, -9F, 0.0F, 0.0F, 0.0F));
        PartDefinition mouth = nose.addOrReplaceChild("mouth", CubeListBuilder.create()
                        .texOffs(93, 24).addBox(-2F, 0F, -5F, 4F, 2F, 5F),
                PartPose.offsetAndRotation(0F, 2F, 0F, Mth.DEG_TO_RAD * 15F, 0.0F, 0.0F));
        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                        .texOffs(100, 71).addBox(-1F, -3F, -1F, 2F, 3F, 2F),
                PartPose.offsetAndRotation(-3F, -5F, -4F, Mth.DEG_TO_RAD * 10F, 0.0F, Mth.DEG_TO_RAD * -5F));
        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                        .texOffs(82, 104).addBox(-1F, -3F, -1F, 2F, 3F, 2F),
                PartPose.offsetAndRotation(3F, -5F, -4F, Mth.DEG_TO_RAD * 10F, 0.0F, Mth.DEG_TO_RAD * 5F));
        PartDefinition back = direwolf.addOrReplaceChild("back", CubeListBuilder.create()
                        .texOffs(0, 29).addBox(-5F, -3F, -3F, 10F, 10F, 18F)
                        .texOffs(56, 0).addBox(0F, -7F, -3F, 0F, 4F, 18F),
                PartPose.offsetAndRotation(0F, -4F, 4F, Mth.DEG_TO_RAD * 5F, 0.0F, 0.0F));
        PartDefinition back_right_leg_1 = back.addOrReplaceChild("back_right_leg_1", CubeListBuilder.create()
                        .texOffs(0, 74).addBox(-3F, 0F, -5F, 4F, 9F, 9F),
                PartPose.offsetAndRotation(-5F, 1F, 8F, Mth.DEG_TO_RAD * 15F, 0.0F, 0.0F));
        PartDefinition back_right_leg_2 = back_right_leg_1.addOrReplaceChild("back_right_leg_2", CubeListBuilder.create()
                        .texOffs(92, 93).addBox(-1F, -1F, -2F, 3F, 8F, 4F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(-1F, 8F, -2F, Mth.DEG_TO_RAD * 45F, 0.0F, 0.0F));
        PartDefinition back_right_leg_3 = back_right_leg_2.addOrReplaceChild("back_right_leg_3", CubeListBuilder.create()
                        .texOffs(64, 96).addBox(-1F, -2F, -2F, 3F, 8F, 3F)
                        .texOffs(100, 64).addBox(0.5F, -2F, 1F, 0F, 4F, 3F),
                PartPose.offsetAndRotation(0F, 7F, -1F, Mth.DEG_TO_RAD * -75F, 0.0F, 0.0F));
        PartDefinition back_right_foot = back_right_leg_3.addOrReplaceChild("back_right_foot", CubeListBuilder.create()
                        .texOffs(61, 8).addBox(-1.5F, -0.025F, -5F, 3F, 2F, 5.25F),
                PartPose.offsetAndRotation(0.5F, 5F, 1F, Mth.DEG_TO_RAD * 15F, 0.0F, 0.0F));
        back_right_foot.addOrReplaceChild("back_right_foot_r7", CubeListBuilder.create()
                        .texOffs(79, 9).addBox(-1.5F, -2F, -4.75F, 2.5F, 2F, 4.75F),
                PartPose.offsetAndRotation(0F, 2F, 0F, 0.0F, Mth.DEG_TO_RAD * 10F, 0.0F));
        back_right_foot.addOrReplaceChild("back_right_foot_r8", CubeListBuilder.create()
                        .texOffs(79, 9).addBox(-1F, -2F, -4.75F, 2.5F, 2F, 4.75F),
                PartPose.offsetAndRotation(0F, 2F, 0F, 0.0F, Mth.DEG_TO_RAD * -10F, 0.0F));
        PartDefinition back_left_leg_1 = back.addOrReplaceChild("back_left_leg_1", CubeListBuilder.create()
                        .texOffs(26, 78).addBox(-1F, 0F, -5F, 4F, 9F, 9F),
                PartPose.offsetAndRotation(5F, 1F, 8F, Mth.DEG_TO_RAD * 15F, 0.0F, 0.0F));
        PartDefinition back_left_leg_2 = back_left_leg_1.addOrReplaceChild("back_left_leg_2", CubeListBuilder.create()
                        .texOffs(22, 96).addBox(-2F, -1F, -2F, 3F, 8F, 4F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(1F, 8F, -2F, Mth.DEG_TO_RAD * 45F, 0.0F, 0.0F));
        PartDefinition back_left_leg_3 = back_left_leg_2.addOrReplaceChild("back_left_leg_3", CubeListBuilder.create()
                        .texOffs(98, 78).addBox(-2F, -2F, -2F, 3F, 8F, 3F)
                        .texOffs(76, 104).addBox(-0.5F, -2F, 1F, 0F, 4F, 3F),
                PartPose.offsetAndRotation(0F, 7F, -1F, Mth.DEG_TO_RAD * -75F, 0.0F, 0.0F));
        PartDefinition back_left_foot = back_left_leg_3.addOrReplaceChild("back_left_foot", CubeListBuilder.create()
                        .texOffs(61, 8).addBox(-1.5F, -0.025F, -5F, 3F, 2F, 5.25F),
                PartPose.offsetAndRotation(-0.5F, 5F, 1F, Mth.DEG_TO_RAD * 15F, 0.0F, 0.0F));
        back_left_foot.addOrReplaceChild("back_left_foot_r9", CubeListBuilder.create()
                        .texOffs(79, 9).addBox(-1F, -2F, -4.75F, 2.5F, 2F, 4.75F),
                PartPose.offsetAndRotation(0F, 2F, 0F, 0.0F, Mth.DEG_TO_RAD * -10F, 0.0F));
        back_left_foot.addOrReplaceChild("back_left_foot_r10", CubeListBuilder.create()
                        .texOffs(79, 9).addBox(-1.5F, -2F, -4.75F, 2.5F, 2F, 4.75F),
                PartPose.offsetAndRotation(0F, 2F, 0F, 0.0F, Mth.DEG_TO_RAD * 10F, 0.0F));
        PartDefinition tail = back.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(56, 40).addBox(-2F, -3F, -1F, 4F, 4F, 14F)
                        .texOffs(0, 57).addBox(-2F, 1F, -1F, 4F, 3F, 14F),
                PartPose.offsetAndRotation(0F, 2F, 15F, Mth.DEG_TO_RAD * -55F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
