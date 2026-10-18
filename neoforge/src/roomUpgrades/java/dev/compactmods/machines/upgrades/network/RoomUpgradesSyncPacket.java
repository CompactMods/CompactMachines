package dev.compactmods.machines.upgrades.network;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.client.ClientRoomUpgrades;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadHandler;

import java.util.List;

/**
 * Server → client: the upgrades enabled on a room (their compiled upgrade ids), in reply to a
 * {@link RoomUpgradesRequestPacket}.
 */
public record RoomUpgradesSyncPacket(String roomCode, List<Identifier> upgrades) implements CustomPacketPayload {

    public static final Type<RoomUpgradesSyncPacket> TYPE =
            new Type<>(CompactMachinesCore.identifier("room_upgrades_sync"));

    public static final StreamCodec<FriendlyByteBuf, RoomUpgradesSyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, RoomUpgradesSyncPacket::roomCode,
            Identifier.STREAM_CODEC.apply(ByteBufCodecs.list()), RoomUpgradesSyncPacket::upgrades,
            RoomUpgradesSyncPacket::new
    );

    // Client store referenced only inside the enqueueWork lambda, so the server never loads it
    public static final IPayloadHandler<RoomUpgradesSyncPacket> HANDLER = (pkt, ctx) ->
            ctx.enqueueWork(() -> ClientRoomUpgrades.accept(pkt.roomCode(), pkt.upgrades()));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
