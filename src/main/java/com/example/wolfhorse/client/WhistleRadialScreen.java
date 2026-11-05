package com.example.wolfhorse.client;

import com.example.wolfhorse.item.WolfWhistleItem;
import com.example.wolfhorse.item.WolfWhistleItem.Mode;
import com.example.wolfhorse.network.ModNetwork;
import com.example.wolfhorse.network.SetWhistleModePacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

/**
 * Ring menu shown while right-click is held with the Wolf Whistle.
 * Point the mouse at a mode and let go of right-click to pick it. Esc cancels.
 */
public class WhistleRadialScreen extends Screen {
    private static final float INNER = 34.0F;
    private static final float OUTER = 96.0F;
    private static final float LABEL_RADIUS = 65.0F;
    private static final float DEADZONE = 18.0F;
    private static final double GAP = 0.03D; // radians between segments

    private final InteractionHand hand;
    private final ItemStack whistle;
    private final int current;
    private int hovered = -1;

    public WhistleRadialScreen(InteractionHand hand, ItemStack whistle) {
        super(Component.translatable("item.wolfhorse.wolf_whistle"));
        this.hand = hand;
        this.whistle = whistle;
        this.current = WolfWhistleItem.getMode(whistle).ordinal();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (!isUseKeyHeld()) {
            this.choose();
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        InputConstants.Key key = Minecraft.getInstance().options.keyUse.getKey();
        if (key.getType() == InputConstants.Type.MOUSE && key.getValue() == button) {
            this.updateHovered(mouseX, mouseY);
            this.choose();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void choose() {
        if (this.minecraft == null || this.minecraft.screen != this) {
            return;
        }
        if (this.hovered >= 0 && this.hovered != this.current) {
            ModNetwork.CHANNEL.sendToServer(new SetWhistleModePacket(this.hand, this.hovered));
        }
        this.onClose();
    }

    private static boolean isUseKeyHeld() {
        Minecraft mc = Minecraft.getInstance();
        InputConstants.Key key = mc.options.keyUse.getKey();
        long window = mc.getWindow().getWindow();
        if (key.getType() == InputConstants.Type.MOUSE) {
            return GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
        }
        return InputConstants.isKeyDown(window, key.getValue());
    }

    private void updateHovered(double mouseX, double mouseY) {
        int n = Mode.values().length;
        double slice = Math.PI * 2.0D / n;
        double dx = mouseX - this.width / 2.0D;
        double dy = mouseY - this.height / 2.0D;
        if (dx * dx + dy * dy < DEADZONE * DEADZONE) {
            this.hovered = -1;
            return;
        }
        // angle 0 = straight up, increasing clockwise, centred on segment 0
        double a = Math.atan2(dy, dx) + Math.PI / 2.0D + slice / 2.0D;
        a = ((a % (Math.PI * 2.0D)) + Math.PI * 2.0D) % (Math.PI * 2.0D);
        this.hovered = Math.min(n - 1, (int) (a / slice));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        this.updateHovered(mouseX, mouseY);
        Mode[] modes = Mode.values();
        int n = modes.length;
        double slice = Math.PI * 2.0D / n;
        float cx = this.width / 2.0F;
        float cy = this.height / 2.0F;

        // --- ring segments ---
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f m = g.pose().last().pose();
        BufferBuilder bb = Tesselator.getInstance().getBuilder();
        bb.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i < n; i++) {
            int argb;
            if (i == this.hovered) {
                argb = 0xE0C8963C;      // gold
            } else if (i == this.current) {
                argb = 0xC0644B32;      // brown = current mode
            } else {
                argb = 0xB0202020;      // dark grey
            }
            int a = argb >>> 24, r = (argb >> 16) & 255, gr = (argb >> 8) & 255, b = argb & 255;
            double start = -Math.PI / 2.0D - slice / 2.0D + i * slice + GAP;
            double end = start + slice - 2.0D * GAP;
            int steps = 20;
            for (int k = 0; k < steps; k++) {
                double a0 = start + (end - start) * k / steps;
                double a1 = start + (end - start) * (k + 1) / steps;
                float c0 = (float) Math.cos(a0), s0 = (float) Math.sin(a0);
                float c1 = (float) Math.cos(a1), s1 = (float) Math.sin(a1);
                bb.vertex(m, cx + c0 * INNER, cy + s0 * INNER, 0).color(r, gr, b, a).endVertex();
                bb.vertex(m, cx + c0 * OUTER, cy + s0 * OUTER, 0).color(r, gr, b, a).endVertex();
                bb.vertex(m, cx + c1 * OUTER, cy + s1 * OUTER, 0).color(r, gr, b, a).endVertex();
                bb.vertex(m, cx + c1 * INNER, cy + s1 * INNER, 0).color(r, gr, b, a).endVertex();
            }
        }
        BufferUploader.drawWithShader(bb.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        // --- labels ---
        for (int i = 0; i < n; i++) {
            double mid = -Math.PI / 2.0D + i * slice;
            int lx = (int) (cx + Math.cos(mid) * LABEL_RADIUS);
            int ly = (int) (cy + Math.sin(mid) * LABEL_RADIUS) - 4;
            int color = i == this.hovered ? 0xFFFFFF80 : 0xFFFFFFFF;
            g.drawCenteredString(this.font, modes[i].label(), lx, ly, color);
        }

        // --- centre: the whistle, and a hint underneath the ring ---
        g.renderItem(this.whistle, (int) cx - 8, (int) cy - 8);
        g.drawCenteredString(this.font, Component.translatable("item.wolfhorse.wolf_whistle.radial_hint"),
                (int) cx, (int) (cy + OUTER + 8), 0xFFAAAAAA);
    }
}
