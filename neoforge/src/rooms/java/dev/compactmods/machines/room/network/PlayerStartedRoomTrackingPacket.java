package dev.compactmods.machines.room.network;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

// TODO: Data Attachment on player

public record PlayerStartedRoomTrackingPacket(String roomCode) implements CustomPacketPayload {

    public static final Type<PlayerStartedRoomTrackingPacket> TYPE = new Type<>(CompactMachinesCore.identifier("player_started_tracking_room"));

    public static final StreamCodec<FriendlyByteBuf, PlayerStartedRoomTrackingPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlayerStartedRoomTrackingPacket::roomCode,
            PlayerStartedRoomTrackingPacket::new
    );

    public static final IPayloadHandler<PlayerStartedRoomTrackingPacket> HANDLER = (pkt, ctx) -> {
        var sender = ctx.player();
        var server = ctx.player().level().getServer();

        final var registry = server.getCapability(RoomCapabilities.REGISTRY);
        if(registry == null)
            return;

//       registry.get(pkt.roomCode).ifPresent(instance -> {
//            final StructureTemplate blocks;
//            try {
//                blocks = RoomBlocks.getInternalBlocks(server, instance).get(5, TimeUnit.SECONDS);
//                PacketDistributor.sendToPlayer(server.getPlayerList().getPlayer(sender.getUUID()),
//                        new InitialRoomBlockDataPacket(blocks));
//
//            } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                throw new RuntimeException(e);
//            }
//        });
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
