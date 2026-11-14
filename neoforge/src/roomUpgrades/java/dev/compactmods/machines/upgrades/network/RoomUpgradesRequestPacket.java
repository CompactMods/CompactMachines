package dev.compactmods.machines.upgrades.network;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.CMFeatureFlags;
import dev.compactmods.machines.upgrades.system.RoomSystems;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.Comparator;

/**
 * Client → server: asks for the upgrades enabled on a room (sent when the machine screen's room
 * upgrades panel opens). Answered with a {@link RoomUpgradesSyncPacket}; unknown rooms, or worlds
 * without the room upgrades feature, get no reply.
 */
public record RoomUpgradesRequestPacket(String roomCode) implements CustomPacketPayload {

    public static final Type<RoomUpgradesRequestPacket> TYPE =
            new Type<>(CompactMachinesCore.identifier("room_upgrades_request"));

    public static final StreamCodec<FriendlyByteBuf, RoomUpgradesRequestPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RoomUpgradesRequestPacket::roomCode,
            RoomUpgradesRequestPacket::new
    );

    public static final IPayloadHandler<RoomUpgradesRequestPacket> HANDLER = (pkt, ctx) -> ctx.enqueueWork(() -> {
        if (!(ctx.player() instanceof ServerPlayer player))
            return;

        final var server = player.level().getServer();
        if (server == null || !CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(server.getWorldData().enabledFeatures()))
            return;

        final var rooms = RoomCapabilities.REGISTRY.getCapability(server);
        if (rooms == null)
            return;

        rooms.get(pkt.roomCode()).ifPresent(room -> {
            final var upgrades = room.getData(RoomSystems.ENABLED_UPGRADES).stream()
                    .map(ResourceKey::identifier)
                    .sorted(Comparator.comparing(Identifier::toString))
                    .toList();
            PacketDistributor.sendToPlayer(player, new RoomUpgradesSyncPacket(room.code(), upgrades));
        });
    });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
