package dev.compactmods.machines.room.network;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.preview.server.RoomPreviewService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.List;

/**
 * Client → server: the full set of room codes the client currently wants live previews for (derived
 * from the machines loaded around the player). Sent as a complete set — it replaces the player's
 * previous subscription — so it is idempotent and cheap to debounce on the client. The server
 * responds with a {@link RoomPreviewSnapshotPacket} for each newly requested, loaded room and keeps
 * pushing updates while the subscription stands.
 */
public record RoomPreviewSubscribePacket(List<String> roomCodes) implements CustomPacketPayload {

    public static final Type<RoomPreviewSubscribePacket> TYPE =
            new Type<>(CompactMachinesCore.identifier("room_preview_subscribe"));

    public static final StreamCodec<FriendlyByteBuf, RoomPreviewSubscribePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), RoomPreviewSubscribePacket::roomCodes,
            RoomPreviewSubscribePacket::new
    );

    public static final IPayloadHandler<RoomPreviewSubscribePacket> HANDLER = (pkt, ctx) -> ctx.enqueueWork(() -> {
        if (ctx.player() instanceof ServerPlayer player)
            RoomPreviewService.setSubscription(player, pkt.roomCodes());
    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
