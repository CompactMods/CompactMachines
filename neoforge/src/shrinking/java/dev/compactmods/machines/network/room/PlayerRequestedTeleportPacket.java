package dev.compactmods.machines.network.room;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.concurrent.TimeUnit;

public record PlayerRequestedTeleportPacket(GlobalPos machine, String room) implements CustomPacketPayload {

    public static final Type<PlayerRequestedTeleportPacket> TYPE = new Type<>(CompactMachinesCore.identifier("player_teleport"));

    public static final StreamCodec<FriendlyByteBuf, PlayerRequestedTeleportPacket> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, PlayerRequestedTeleportPacket::machine,
            ByteBufCodecs.STRING_UTF8, PlayerRequestedTeleportPacket::room,
            PlayerRequestedTeleportPacket::new
    );

    public static final IPayloadHandler<PlayerRequestedTeleportPacket> HANDLER = (pkt, ctx) -> {
        ctx.enqueueWork(() -> {
            final var player = ctx.player();
            if (player instanceof ServerPlayer sp) {
                final var server = player.level().getServer();
                server.getCapability(RoomCapabilities.REGISTRY)
                        .get(pkt.room)
                        .ifPresent(room -> {
                            var handler = sp.getCapability(Shrinking.SHRINK, room);
                            assert handler != null;
                            handler.tryExit();
                        });
            }
        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
