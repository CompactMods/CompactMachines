package dev.compactmods.machines.neoforge.network;

import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.neoforge.room.client.ClientRoomPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPlayPayloadHandler;

import java.util.UUID;

public record SyncRoomMetadataPacket(String roomCode, UUID owner) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "sync_room_metadata");

    public static final FriendlyByteBuf.Reader<SyncRoomMetadataPacket> READER = (buf) -> new SyncRoomMetadataPacket(buf.readUtf(), buf.readUUID());

    public static final IPlayPayloadHandler<SyncRoomMetadataPacket> HANDLER = (pkt, ctx) -> {
        ClientRoomPacketHandler.handleRoomSync(pkt.roomCode, pkt.owner);
    };

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(roomCode);
        buffer.writeUUID(owner);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
