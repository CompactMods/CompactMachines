package dev.compactmods.machines.forgebuiltin.tunnel.definitions.redstone;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.redstone.RedstoneReaderTunnel;
import net.minecraft.core.GlobalPos;
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
    public int powerLevel(MinecraftServer server, GlobalPos machine, TunnelPosition tunnel) {
        LevelReader connectedWorld = server.getLevel(machine.dimension());
        if (connectedWorld != null) {
            final var relPos = machine.pos().relative(tunnel.machineSide()).immutable();
            BlockState state = connectedWorld.getBlockState(relPos);
            return state.getSignal(connectedWorld, relPos, tunnel.machineSide());
        }

        return 0;
    }
}

