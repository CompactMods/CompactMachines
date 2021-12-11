package dev.compactmods.machines.tunnels.definitions;

import dev.compactmods.machines.api.teleportation.IDimensionalPosition;
import dev.compactmods.machines.api.tunnels.EnumTunnelSide;
import dev.compactmods.machines.api.tunnels.ITunnelConnectionInfo;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import dev.compactmods.machines.api.tunnels.redstone.IRedstoneReaderTunnel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelReader;
import net.minecraft.server.level.ServerLevel;

import java.awt.*;
import java.util.Optional;

public class RedstoneInTunnelDefinition extends TunnelDefinition implements IRedstoneReaderTunnel {

    @Override
    public int getTunnelRingColor() {
        return new Color(167, 38, 38).getRGB();
    }

    @Override
    public int getTunnelIndicatorColor() {
        return Color.blue.getRGB();
        // return Color.ORANGE.darker().getRGB();
    }

    @Override
    public int getPowerLevel(ITunnelConnectionInfo connectionInfo) {
        LevelReader connectedWorld = connectionInfo.getConnectedWorld(EnumTunnelSide.OUTSIDE).orElse(null);
        if (connectedWorld instanceof ServerLevel) {
            IDimensionalPosition pos = connectionInfo.getConnectedPosition(EnumTunnelSide.OUTSIDE).orElse(null);
            if (pos == null)
                return 0;

            Optional<BlockState> state = connectionInfo.getConnectedState(EnumTunnelSide.OUTSIDE);
            if (!state.isPresent()) return 0;

            int weak = state.get().getSignal(
                    connectedWorld,
                    pos.getBlockPosition(),
                    connectionInfo.getConnectedSide(EnumTunnelSide.OUTSIDE));

            return weak;
        }

        return 0;
    }
}

