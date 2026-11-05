package com.example.wolfhorse.client;

import com.example.wolfhorse.WolfHorseMod;
import com.example.wolfhorse.item.WolfWhistleItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Opens the whistle's radial menu once right-click has been held for a moment. */
@Mod.EventBusSubscriber(modid = WolfHorseMod.MODID, value = Dist.CLIENT)
public class WhistleClientEvents {
    /** Set once the menu has opened for the current hold, so it doesn't reopen until you let go. */
    private static boolean openedThisHold = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        boolean usingWhistle = player.isUsingItem() && player.getUseItem().getItem() instanceof WolfWhistleItem;
        if (!usingWhistle) {
            openedThisHold = false;
            return;
        }
        if (openedThisHold || mc.screen != null) {
            return;
        }
        if (player.getTicksUsingItem() >= WolfWhistleItem.RADIAL_OPEN_TICKS) {
            openedThisHold = true;
            mc.setScreen(new WhistleRadialScreen(player.getUsedItemHand(), player.getUseItem().copy()));
        }
    }
}
