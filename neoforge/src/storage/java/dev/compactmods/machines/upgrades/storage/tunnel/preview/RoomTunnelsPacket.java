package dev.compactmods.machines.upgrades.storage.tunnel.preview;

import dev.compactmods.machines.core.CompactMachinesCore;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.List;

/**
 * Server → client: a watched room's tunnels, for the room preview screen. Sent by
 * {@link TunnelPreviewContributor} when they change and when the player starts watching the room.
 */
public record RoomTunnelsPacket(String room, List<TunnelMarker> tunnels) implements CustomPacketPayload {

    public static final Type<RoomTunnelsPacket> TYPE = new Type<>(CompactMachinesCore.identifier("room_tunnels"));

    public static final StreamCodec<ByteBuf, RoomTunnelsPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RoomTunnelsPacket::room,
            TunnelMarker.STREAM_CODEC.apply(ByteBufCodecs.list()), RoomTunnelsPacket::tunnels,
            RoomTunnelsPacket::new);

    public static final IPayloadHandler<RoomTunnelsPacket> HANDLER = (pkt, ctx) ->
            ctx.enqueueWork(() -> ClientTunnelMarkers.accept(pkt.room(), pkt.tunnels()));

    public RoomTunnelsPacket {
        tunnels = List.copyOf(tunnels);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
