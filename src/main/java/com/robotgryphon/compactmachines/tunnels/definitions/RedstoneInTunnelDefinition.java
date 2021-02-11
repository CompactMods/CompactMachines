package com.robotgryphon.compactmachines.tunnels.definitions;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.api.tunnels.EnumTunnelSide;
import com.robotgryphon.compactmachines.api.tunnels.ITunnelConnectionInfo;
import com.robotgryphon.compactmachines.api.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import com.robotgryphon.compactmachines.api.tunnels.redstone.IRedstoneReaderTunnel;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.server.ServerWorld;

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
        IWorldReader connectedWorld = connectionInfo.getConnectedWorld(EnumTunnelSide.OUTSIDE).orElse(null);
        if (connectedWorld instanceof ServerWorld) {
            DimensionalPosition pos = connectionInfo.getConnectedPosition(EnumTunnelSide.OUTSIDE).orElse(null);
            if (pos == null)
                return 0;

            Optional<BlockState> state = connectionInfo.getConnectedState(EnumTunnelSide.OUTSIDE);
            if (!state.isPresent()) return 0;

            int weak = state.get().getWeakPower(
                    connectedWorld,
                    pos.getBlockPosition(),
                    connectionInfo.getConnectedSide(EnumTunnelSide.OUTSIDE));

            return weak;
        }

        return 0;
    }
}

