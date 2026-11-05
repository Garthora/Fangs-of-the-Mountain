package com.example.wolfhorse.network;

import com.example.wolfhorse.item.WolfWhistleItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Sent when the player picks a mode on the whistle's radial menu. */
public record SetWhistleModePacket(InteractionHand hand, int mode) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeVarInt(this.mode);
    }

    public static SetWhistleModePacket decode(FriendlyByteBuf buf) {
        return new SetWhistleModePacket(buf.readEnum(InteractionHand.class), buf.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ServerPlayer player = ctx.get().getSender();
        if (player == null) {
            return;
        }
        ItemStack stack = player.getItemInHand(this.hand);
        if (stack.getItem() instanceof WolfWhistleItem) {
            WolfWhistleItem.selectMode(stack, player, this.mode);
        }
    }
}
