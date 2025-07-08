package net.willo678.filingcabinet.network;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.willo678.filingcabinet.screen.FilingCabinetScreen;

import java.util.function.Supplier;

public class ServerToClientStoragePacket {
    public CompoundTag tag;

    public ServerToClientStoragePacket(CompoundTag tag) {
        this.tag = tag;
    }

    public ServerToClientStoragePacket(FriendlyByteBuf pb) {
        tag = pb.readAnySizeNbt();
    }

    public void toBytes(FriendlyByteBuf pb) {
        pb.writeNbt(tag);
    }

    public static class Handler {

        public static boolean onMessage(ServerToClientStoragePacket message, Supplier<NetworkEvent.Context> ctx) {
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                ctx.get().enqueueWork(() -> {
                    if (Minecraft.getInstance().screen instanceof FilingCabinetScreen terminalScreen) {
                        terminalScreen.receive(message.tag);
                    }
                });
            }
            ctx.get().setPacketHandled(true);
            return true;
        }
    }
}
