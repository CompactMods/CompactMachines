package dev.compactmods.machines.api.tunnels.redstone;

import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import net.minecraft.server.MinecraftServer;

public interface RedstoneReaderTunnel extends RedstoneTunnel {

    int powerLevel(MinecraftServer server, IDimensionalBlockPosition machine, TunnelPosition tunnel);

}
