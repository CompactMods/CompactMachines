package dev.compactmods.machines.upgrades.storage.tunnel.network;

import dev.compactmods.machines.upgrades.storage.tunnel.preview.RoomTunnelsPacket;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/// Tunnels' network handler; invoked by the mod's central payload-registration listener.
public final class TunnelNetworking {
    private TunnelNetworking() {}

    public static void register(final PayloadRegistrar reg) {
        reg.playToServer(TunnelConfigSubmitPacket.TYPE, TunnelConfigSubmitPacket.STREAM_CODEC, TunnelConfigSubmitPacket.HANDLER);
        reg.playToClient(TunnelConfigStatePacket.TYPE, TunnelConfigStatePacket.STREAM_CODEC, TunnelConfigStatePacket.HANDLER);
        reg.playToClient(RoomTunnelsPacket.TYPE, RoomTunnelsPacket.STREAM_CODEC, RoomTunnelsPacket.HANDLER);
    }
}
