package dev.compactmods.machines.tunnel.definitions.redstone;

import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.redstone.RedstoneReaderTunnel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import java.awt.*;

public class RedstoneInTunnelDefinition implements RedstoneReaderTunnel, TunnelDefinition {

    @Override
    public int ringColor() {
        return new Color(167, 38, 38).getRGB();
    }

    @Override
    public int indicatorColor() {
        return Color.blue.getRGB();
        // return Color.ORANGE.darker().getRGB();
    }

    @Override
    public int powerLevel(MinecraftServer server, IDimensionalBlockPosition machine, TunnelPosition tunnel) {
        LevelReader connectedWorld = machine.level(server);
        if (connectedWorld != null) {
            BlockState state = machine.state(server);
            return state.getSignal(connectedWorld, machine.getBlockPosition(), tunnel.machineSide());
        }

        return 0;
    }
}

