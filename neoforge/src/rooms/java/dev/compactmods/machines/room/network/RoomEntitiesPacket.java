package dev.compactmods.machines.room.network;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.preview.RoomEntitySnapshot;
import dev.compactmods.machines.room.preview.client.ClientRoomEntities;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.List;

/**
 * Server → client: the current entities inside a tracked room, in room-local space. Sent frequently
 * (real-time) and separately from the slow {@link RoomPreviewSnapshotPacket block snapshot}, so entity
 * motion stays live without re-shipping block geometry. The client replaces its stored set for the
 * room each time.
 */
public record RoomEntitiesPacket(String roomCode, List<RoomEntitySnapshot> entities) implements CustomPacketPayload {

    public static final Type<RoomEntitiesPacket> TYPE =
            new Type<>(CompactMachinesCore.identifier("room_preview_entities"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RoomEntitiesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RoomEntitiesPacket::roomCode,
            RoomEntitySnapshot.LIST_STREAM_CODEC, RoomEntitiesPacket::entities,
            RoomEntitiesPacket::new
    );

    public static final IPayloadHandler<RoomEntitiesPacket> HANDLER = (pkt, ctx) ->
            ctx.enqueueWork(() -> ClientRoomEntities.accept(pkt.roomCode(), pkt.entities()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
