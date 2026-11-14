package dev.compactmods.machines.upgrades.network;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/// Room upgrades feature's network handler; invoked by the mod's central payload-registration listener.
public final class RoomUpgradesNetworking {
    private RoomUpgradesNetworking() {}

    public static void register(final PayloadRegistrar reg) {
        reg.playToClient(RoomUpgradesSyncPacket.TYPE, RoomUpgradesSyncPacket.STREAM_CODEC, RoomUpgradesSyncPacket.HANDLER);
        reg.playToServer(RoomUpgradesRequestPacket.TYPE, RoomUpgradesRequestPacket.STREAM_CODEC, RoomUpgradesRequestPacket.HANDLER);
    }
}
