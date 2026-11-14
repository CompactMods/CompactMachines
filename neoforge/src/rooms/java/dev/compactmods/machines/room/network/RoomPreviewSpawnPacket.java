package dev.compactmods.machines.room.network;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.preview.client.ClientRoomPreviews;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.joml.Vector3fc;

/**
 * Server → client: where the receiving player spawns in a room they're previewing — position in the
 * preview's room-local coordinates (blocks from the interior's minimum corner), plus the yaw/pitch
 * (degrees) they face on arrival. Per player — unlike the shared
 * snapshot — since each player can have their own spawn. Sent when the player starts watching the
 * room and whenever their spawn there changes.
 */
public record RoomPreviewSpawnPacket(String roomCode, Vector3fc localSpawn, float yaw, float pitch) implements CustomPacketPayload {

    public static final Type<RoomPreviewSpawnPacket> TYPE =
            new Type<>(CompactMachinesCore.identifier("room_preview_spawn"));

    public static final StreamCodec<FriendlyByteBuf, RoomPreviewSpawnPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RoomPreviewSpawnPacket::roomCode,
            ByteBufCodecs.VECTOR3F, RoomPreviewSpawnPacket::localSpawn,
            ByteBufCodecs.FLOAT, RoomPreviewSpawnPacket::yaw,
            ByteBufCodecs.FLOAT, RoomPreviewSpawnPacket::pitch,
            RoomPreviewSpawnPacket::new
    );

    // Client store referenced only inside the enqueueWork lambda (see RoomPreviewSnapshotPacket)
    public static final IPayloadHandler<RoomPreviewSpawnPacket> HANDLER = (pkt, ctx) ->
            ctx.enqueueWork(() -> ClientRoomPreviews.acceptSpawn(pkt.roomCode(),
                    new ClientRoomPreviews.Spawn(pkt.localSpawn(), pkt.yaw(), pkt.pitch())));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
