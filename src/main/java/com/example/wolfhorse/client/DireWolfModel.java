package com.example.wolfhorse.client;

import com.example.wolfhorse.entity.DireWolfBreed;
import com.example.wolfhorse.entity.WolfHorseEntity;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.horse.Variant;

/**
 * Wolf-shaped model for the DireWolf. Uses the vanilla wolf geometry (baked from the
 * vanilla wolf layer) and copies the wolf's walk and sit animations. Fur colour is a tint
 * applied on top of the vanilla wolf texture.
 */
public class DireWolfModel extends AgeableListModel<WolfHorseEntity> {
    private final ModelPart head;
    final ModelPart body;
    private final ModelPart rightHindLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart tail;
    private final ModelPart upperBody;

    private float tintR = 1.0F, tintG = 1.0F, tintB = 1.0F;
    /** Turned off while drawing the glowing eyes, so they stay bright red. */
    boolean useTint = true;

    public DireWolfModel(ModelPart root) {
        super(false, 5.0F, 2.0F);
        this.head = root.getChild("head");
        this.body = root.getChild("body");
        this.rightHindLeg = root.getChild("right_hind_leg");
        this.leftHindLeg = root.getChild("left_hind_leg");
        this.rightFrontLeg = root.getChild("right_front_leg");
        this.leftFrontLeg = root.getChild("left_front_leg");
        this.tail = root.getChild("tail");
        this.upperBody = root.getChild("upper_body");
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return ImmutableList.of(this.head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return ImmutableList.of(this.body, this.rightHindLeg, this.leftHindLeg,
                this.rightFrontLeg, this.leftFrontLeg, this.tail, this.upperBody);
    }

    @Override
    public void prepareMobModel(WolfHorseEntity entity, float limbSwing, float limbSwingAmount, float partialTick) {
        float[] tint = tintFor(entity.getBreed(), entity.getVariant());
        this.tintR = tint[0];
        this.tintG = tint[1];
        this.tintB = tint[2];

        this.tail.yRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;

        if (entity.isOrderedToSit() && !entity.isVehicle()) {
            this.upperBody.setPos(-1.0F, 16.0F, -3.0F);
            this.upperBody.xRot = 1.2566371F;
            this.upperBody.yRot = 0.0F;
            this.body.setPos(0.0F, 18.0F, 0.0F);
            this.body.xRot = (float) Math.PI / 4F;
            this.tail.setPos(-1.0F, 21.0F, 6.0F);
            this.rightHindLeg.setPos(-2.5F, 22.7F, 2.0F);
            this.rightHindLeg.xRot = (float) Math.PI * 1.5F;
            this.leftHindLeg.setPos(0.5F, 22.7F, 2.0F);
            this.leftHindLeg.xRot = (float) Math.PI * 1.5F;
            this.rightFrontLeg.xRot = 5.811947F;
            this.rightFrontLeg.setPos(-2.49F, 17.0F, -4.0F);
            this.leftFrontLeg.xRot = 5.811947F;
            this.leftFrontLeg.setPos(0.51F, 17.0F, -4.0F);
        } else {
            this.body.setPos(0.0F, 14.0F, 2.0F);
            this.body.xRot = (float) Math.PI / 2F;
            this.upperBody.setPos(-1.0F, 14.0F, -3.0F);
            this.upperBody.xRot = this.body.xRot;
            this.tail.setPos(-1.0F, 12.0F, 8.0F);
            this.rightHindLeg.setPos(-2.5F, 16.0F, 7.0F);
            this.leftHindLeg.setPos(0.5F, 16.0F, 7.0F);
            this.rightFrontLeg.setPos(-2.5F, 16.0F, -4.0F);
            this.leftFrontLeg.setPos(0.5F, 16.0F, -4.0F);
            this.rightHindLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
            this.leftHindLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
            this.rightFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
            this.leftFrontLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
        }
    }

    @Override
    public void setupAnim(WolfHorseEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        // Tamed ones carry their tail higher, like wolves
        this.tail.xRot = entity.isTamed() ? 1.2F : 0.6283185F;
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

    /** Fur colour per coat. The same "grey" coat is lighter on Tundra and darker on Plains. */
    static float[] tintFor(DireWolfBreed breed, Variant coat) {
        return switch (coat) {
            case WHITE -> new float[]{1.0F, 1.0F, 1.0F};
            case CREAMY -> new float[]{0.95F, 0.80F, 0.58F};
            case CHESTNUT -> new float[]{0.78F, 0.50F, 0.30F};
            case BROWN -> new float[]{0.62F, 0.43F, 0.28F};
            case DARK_BROWN -> new float[]{0.40F, 0.27F, 0.18F};
            case BLACK -> new float[]{0.20F, 0.20F, 0.22F};
            case GRAY -> breed == DireWolfBreed.TUNDRA
                    ? new float[]{0.68F, 0.68F, 0.70F}
                    : new float[]{0.38F, 0.38F, 0.40F};
        };
    }
}
