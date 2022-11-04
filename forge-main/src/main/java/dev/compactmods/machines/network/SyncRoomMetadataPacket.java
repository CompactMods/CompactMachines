package dev.compactmods.machines.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public record SyncRoomMetadataPacket(String roomCode, UUID owner) {
    public SyncRoomMetadataPacket(FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readUUID());
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(roomCode);
        buffer.writeUUID(owner);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        ClientRoomNetworkHandler.handleRoomSync(this);
        contextSupplier.get().setPacketHandled(true);
    }
}
