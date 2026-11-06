package com.example.wolfhorse.client;

import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.item.FurVariant;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;

/**
 * The Scarf armor trim: a fur scarf wrapped around the neck, with one end pulled through
 * the knot and hanging down the chest. Drawn when the worn helmet has the Scarf trim;
 * the fur used as the trim material picks the colour.
 */
public class ScarfLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(WolfHorseMod.MODID, "scarf"), "main");

    private final ModelPart scarf;

    public ScarfLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
        super(parent);
        this.scarf = models.bakeLayer(LAYER).getChild("scarf");
    }

    /** Coordinates are the body's: neck at y=0, chest faces -z. Sized to sit outside a chestplate. */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("scarf", CubeListBuilder.create()
                        // the wrap around the neck and shoulders
                        .texOffs(0, 0).addBox(-4.5F, -1.0F, -2.5F, 9.0F, 2.5F, 5.0F, new CubeDeformation(0.9F))
                        // the knot, a little off-centre on the chest
                        .texOffs(0, 10).addBox(-3.5F, 0.5F, -4.4F, 3.0F, 2.5F, 1.2F, new CubeDeformation(0.1F))
                        // the end pulled through the knot, hanging down
                        .texOffs(10, 10).addBox(-3.0F, 2.5F, -4.1F, 2.0F, 6.0F, 0.8F)
                        // the shorter loop end peeking out beside it
                        .texOffs(18, 10).addBox(-1.2F, 2.0F, -3.9F, 2.0F, 4.0F, 0.7F),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 32, 32);
    }

    public static ResourceLocation texture(FurVariant variant) {
        return new ResourceLocation(WolfHorseMod.MODID, "textures/entity/scarf_" + variant.id + ".png");
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.isEmpty()) {
            return;
        }
        var trim = ArmorTrim.getTrim(entity.level().registryAccess(), helmet);
        if (trim.isEmpty() || !trim.get().pattern().is(WolfHorseMod.SCARF_TRIM)) {
            return;
        }
        FurVariant variant = FurVariant.fromTrim(trim.get());
        pose.pushPose();
        this.getParentModel().body.translateAndRotate(pose);
        VertexConsumer vc = buffers.getBuffer(RenderType.entityCutoutNoCull(texture(variant)));
        this.scarf.render(pose, vc, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
