package com.example.wolfhorse.client;

import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.item.FurVariant;
import com.example.wolfhorse.item.PeltItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;

import javax.annotation.Nullable;

/**
 * Draws the Pelt on the wearer's back: the folded elytra shape, swinging like a cape
 * (same motion maths as the vanilla cape: it trails behind when you move, bounces as you
 * walk and lifts when you fall).
 */
public class PeltLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    /** textures/entity/pelt_plains.png, pelt_forest.png, pelt_tundra.png */
    public static ResourceLocation texture(FurVariant variant) {
        return new ResourceLocation(WolfHorseMod.MODID, "textures/entity/pelt_" + variant.id + ".png");
    }

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
        FurVariant variant;
        if (stack.getItem() instanceof PeltItem pelt) {
            variant = pelt.getVariant();
        } else {
            variant = stack.is(Items.ELYTRA) ? null : peltTrimVariant(entity);
            if (variant == null) {
                return;
            }
        }
        pose.pushPose();
        pose.translate(0.0F, 0.0F, 0.125F);
        if (entity instanceof Player player) {
            applyCapeSwing(pose, player, partialTick);
        }
        this.getParentModel().copyPropertiesTo(this.model);
        this.model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        VertexConsumer vc = ItemRenderer.getArmorFoilBuffer(buffers,
                RenderType.armorCutoutNoCull(texture(variant)), false, stack.hasFoil());
        this.model.renderToBuffer(pose, vc, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        pose.popPose();
    }

    /** Fur colour of the first worn armor piece with the Pelt trim, or null if none has it. */
    @Nullable
    private static FurVariant peltTrimVariant(LivingEntity entity) {
        RegistryAccess access = entity.level().registryAccess();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack piece = entity.getItemBySlot(slot);
            if (piece.isEmpty()) {
                continue;
            }
            var trim = ArmorTrim.getTrim(access, piece);
            if (trim.isPresent() && trim.get().pattern().is(WolfHorseMod.PELT_TRIM)) {
                return FurVariant.fromTrim(trim.get());
            }
        }
        return null;
    }

    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /** Vanilla cape physics, rotating the whole pelt around the shoulders. */
    private static void applyCapeSwing(PoseStack pose, Player p, float pt) {
        double dx = Mth.lerp(pt, p.xCloakO, p.xCloak) - Mth.lerp(pt, p.xo, p.getX());
        double dy = Mth.lerp(pt, p.yCloakO, p.yCloak) - Mth.lerp(pt, p.yo, p.getY());
        double dz = Mth.lerp(pt, p.zCloakO, p.zCloak) - Mth.lerp(pt, p.zo, p.getZ());
        float bodyYaw = Mth.rotLerp(pt, p.yBodyRotO, p.yBodyRot);
        double sin = Mth.sin(bodyYaw * ((float) Math.PI / 180F));
        double cos = -Mth.cos(bodyYaw * ((float) Math.PI / 180F));

        float lift = Mth.clamp((float) dy * 10.0F, -6.0F, 32.0F);
        float back = Mth.clamp((float) (dx * sin + dz * cos) * 100.0F, 0.0F, 150.0F);
        float side = Mth.clamp((float) (dx * cos - dz * sin) * 100.0F, -20.0F, 20.0F);
        float bob = Mth.lerp(pt, p.oBob, p.bob);
        lift += Mth.sin(Mth.lerp(pt, p.walkDistO, p.walkDist) * 6.0F) * 32.0F * bob;

        // The elytra shape already leans out ~15 degrees and handles crouching itself,
        // so only the movement part of the cape swing is added here.
        pose.mulPose(Axis.XP.rotationDegrees(back / 2.0F + lift));
        pose.mulPose(Axis.ZP.rotationDegrees(side / 2.0F));
    }
}
