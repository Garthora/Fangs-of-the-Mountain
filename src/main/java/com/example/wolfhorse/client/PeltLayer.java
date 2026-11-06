package com.example.wolfhorse.client;

import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.item.PeltItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ElytraModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Draws the Pelt on the wearer's back using the vanilla elytra shape (always folded). */
public class PeltLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(WolfHorseMod.MODID, "textures/entity/pelt.png");

    private final ElytraModel<T> model;

    public PeltLayer(RenderLayerParent<T, M> parent, EntityModelSet models) {
        super(parent);
        this.model = new ElytraModel<>(models.bakeLayer(ModelLayers.ELYTRA));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ItemStack stack = entity.getItemBySlot(EquipmentSlot.CHEST);
        if (!(stack.getItem() instanceof PeltItem)) {
            return;
        }
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 0.125F);
        this.getParentModel().copyPropertiesTo(this.model);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(buffers,
                RenderType.armorCutoutNoCull(TEXTURE), false, stack.hasFoil());
        this.model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }
}
