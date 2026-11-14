package dev.compactmods.machines.room.network;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

public record PlayerRequestedRoomUIPacket(String roomCode) implements CustomPacketPayload {
    public static final Type<PlayerRequestedRoomUIPacket> TYPE = new Type<>(CompactMachinesCore.identifier("player_wants_to_open_room_ui"));

    public static final StreamCodec<FriendlyByteBuf, PlayerRequestedRoomUIPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerRequestedRoomUIPacket::roomCode,
            PlayerRequestedRoomUIPacket::new
    );

    public static final IPayloadHandler<PlayerRequestedRoomUIPacket> HANDLER = (pkt, ctx) -> {
        final var player = ctx.player();
        final var server = player.level().getServer();

        final var registry = server.getCapability(RoomCapabilities.REGISTRY);
        assert registry != null;

        registry.get(pkt.roomCode).ifPresent(inst -> {
            // TODO
        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
