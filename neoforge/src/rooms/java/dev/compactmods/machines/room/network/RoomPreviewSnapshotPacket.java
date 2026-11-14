package dev.compactmods.machines.room.network;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.preview.client.ClientRoomPreviews;
import dev.compactmods.machines.room.preview.RoomPreviewSnapshot;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Server → client: the latest interior {@link RoomPreviewSnapshot} for a room the client is
 * subscribed to. Sent by the server gatherer whenever a subscribed, loaded room's contents change
 * (and once immediately on subscribe). The client stores it keyed by room code for the preview
 * renderer to mesh.
 */
public record RoomPreviewSnapshotPacket(String roomCode, RoomPreviewSnapshot snapshot) implements CustomPacketPayload {

    public static final Type<RoomPreviewSnapshotPacket> TYPE =
            new Type<>(CompactMachinesCore.identifier("room_preview_snapshot"));

    public static final StreamCodec<FriendlyByteBuf, RoomPreviewSnapshotPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RoomPreviewSnapshotPacket::roomCode,
            RoomPreviewSnapshot.STREAM_CODEC, RoomPreviewSnapshotPacket::snapshot,
            RoomPreviewSnapshotPacket::new
    );

    // The client handler is referenced only inside the nested enqueueWork lambda so the client-only
    // ClientRoomPreviews class is never linked when this payload type is registered on a server.
    public static final IPayloadHandler<RoomPreviewSnapshotPacket> HANDLER = (pkt, ctx) ->
            ctx.enqueueWork(() -> ClientRoomPreviews.accept(pkt.roomCode(), pkt.snapshot()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
