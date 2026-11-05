package com.example.wolfhorse.network;

import com.example.wolfhorse.WolfHorseMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private static final String VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(WolfHorseMod.MODID, "main"),
            () -> VERSION, VERSION::equals, VERSION::equals);

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(SetWhistleModePacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SetWhistleModePacket::encode)
                .decoder(SetWhistleModePacket::decode)
                .consumerMainThread(SetWhistleModePacket::handle)
                .add();
    }
}
