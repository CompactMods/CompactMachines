package dev.compactmods.machines.forge.network;

import dev.compactmods.machines.forge.room.client.ClientRoomPacketHandler;
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
        ClientRoomPacketHandler.handleRoomSync(this.roomCode, this.owner);
        contextSupplier.get().setPacketHandled(true);
    }
}
