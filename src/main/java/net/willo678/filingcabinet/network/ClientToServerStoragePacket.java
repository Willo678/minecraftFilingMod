package net.willo678.filingcabinet.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.willo678.filingcabinet.screen.FilingCabinetMenu;

import java.util.function.Supplier;

public class ClientToServerStoragePacket {
    public CompoundTag tag;

    public ClientToServerStoragePacket(CompoundTag tag) {
        this.tag = tag;
    }

    public ClientToServerStoragePacket(FriendlyByteBuf pb) {
        tag = pb.readAnySizeNbt();
    }

    public void toBytes(FriendlyByteBuf pb) {
        pb.writeNbt(tag);
    }

    public static class Handler {

        public static boolean onMessage(ClientToServerStoragePacket message, Supplier<NetworkEvent.Context> ctx) {
            if (ctx.get().getDirection() == NetworkDirection.PLAY_TO_SERVER) {
                ctx.get().enqueueWork(() -> {
                    ServerPlayer sender = ctx.get().getSender();
                    if (sender!=null && sender.containerMenu instanceof FilingCabinetMenu terminalScreen){
                        terminalScreen.receive(message.tag);
                    }
                });
            }
            ctx.get().setPacketHandled(true);
            return true;
        }
    }
}
