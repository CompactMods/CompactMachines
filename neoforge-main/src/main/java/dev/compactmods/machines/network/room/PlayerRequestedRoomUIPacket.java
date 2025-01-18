package dev.compactmods.machines.network.room;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record PlayerRequestedRoomUIPacket(String roomCode) implements CustomPacketPayload {
    public static final Type<PlayerRequestedRoomUIPacket> TYPE = new Type<>(CompactMachines.modRL("player_wants_to_open_room_ui"));

    public static final StreamCodec<FriendlyByteBuf, PlayerRequestedRoomUIPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerRequestedRoomUIPacket::roomCode,
            PlayerRequestedRoomUIPacket::new
    );

    public static final IPayloadHandler<PlayerRequestedRoomUIPacket> HANDLER = (pkt, ctx) -> {
        final var player = ctx.player();
        CompactMachines.room(pkt.roomCode).ifPresent(inst -> {

        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
