package dev.compactmods.machines.room.network;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.preview.RoomBlockEntitySnapshot;
import dev.compactmods.machines.room.preview.client.ClientRoomPreviews;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.List;

/**
 * Server → client: block entities in a watched room whose sync tag changed since last sent. Sent
 * every tick there are changes, like the per-tick packets mods send world clients (Mekanism's
 * transporter batches, for example), so client-side animation in the preview has the data it needs.
 * Blocks themselves, and block entities appearing or going away, still come with the slower
 * {@link RoomPreviewSnapshotPacket snapshot}.
 */
public record RoomBlockEntitiesPacket(String roomCode, List<RoomBlockEntitySnapshot> changed) implements CustomPacketPayload {

    public static final Type<RoomBlockEntitiesPacket> TYPE =
            new Type<>(CompactMachinesCore.identifier("room_preview_block_entities"));

    public static final StreamCodec<ByteBuf, RoomBlockEntitiesPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RoomBlockEntitiesPacket::roomCode,
            RoomBlockEntitySnapshot.STREAM_CODEC.apply(ByteBufCodecs.list()), RoomBlockEntitiesPacket::changed,
            RoomBlockEntitiesPacket::new);

    public static final IPayloadHandler<RoomBlockEntitiesPacket> HANDLER = (pkt, ctx) ->
            ctx.enqueueWork(() -> ClientRoomPreviews.acceptBlockEntities(pkt.roomCode(), pkt.changed()));

    public RoomBlockEntitiesPacket {
        changed = List.copyOf(changed);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
