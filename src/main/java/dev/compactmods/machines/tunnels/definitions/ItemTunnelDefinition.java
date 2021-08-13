package dev.compactmods.machines.tunnels.definitions;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.block.tiles.TunnelWallTile;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import dev.compactmods.machines.api.tunnels.EnumTunnelSide;
import dev.compactmods.machines.tunnels.TunnelHelper;
import dev.compactmods.machines.api.tunnels.IItemTunnel;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.Optional;

public class ItemTunnelDefinition extends TunnelDefinition implements IItemTunnel {

    @Override
    public int getTunnelRingColor() {
        return new Color(205, 143, 36).getRGB();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getInternalCapability(ServerWorld compactWorld, BlockPos tunnelPos, @Nonnull Capability<T> cap, Direction side) {
        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.empty();

        TileEntity te = compactWorld.getBlockEntity(tunnelPos);
        if (te instanceof TunnelWallTile) {
            TunnelWallTile twt = (TunnelWallTile) te;

            Optional<BlockState> connectedState = TunnelHelper.getConnectedState(twt, EnumTunnelSide.INSIDE);
            if (!connectedState.isPresent())
                return LazyOptional.empty();

            Optional<DimensionalPosition> tunnelConnectedPosition = TunnelHelper.getTunnelConnectedPosition(twt, EnumTunnelSide.INSIDE);
            if (!tunnelConnectedPosition.isPresent())
                return LazyOptional.empty();

            Direction tunnelSide = twt.getTunnelSide();

            DimensionalPosition connectedInsidePos = tunnelConnectedPosition.get();
            if (connectedState.get().hasTileEntity()) {
                TileEntity connectedTile = compactWorld.getBlockEntity(connectedInsidePos.getBlockPosition());
                if (connectedTile != null) {
                    return connectedTile.getCapability(cap, tunnelSide);
                }
            }
        }

        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getExternalCapability(ServerWorld world, BlockPos tunnelPos, @Nonnull Capability<T> cap, @Nullable Direction side) {
        TileEntity te = world.getBlockEntity(tunnelPos);

        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.empty();

        if (te instanceof TunnelWallTile) {
            TunnelWallTile twt = (TunnelWallTile) te;

            Optional<BlockState> connectedState = TunnelHelper.getConnectedState(twt, EnumTunnelSide.OUTSIDE);
            if (!connectedState.isPresent())
                return LazyOptional.empty();

            // link to external block capability
            Optional<DimensionalPosition> connectedPosition = twt.getConnectedPosition();
            if (!connectedPosition.isPresent())
                return LazyOptional.empty();

            DimensionalPosition dimensionalPosition = connectedPosition.get();
            // CompactMachines.LOGGER.debug(String.format("[%s] %s %s", 0, dimensionalPosition.getDimension(), dimensionalPosition.getPosition()));

            Optional<ServerWorld> connectedWorld = dimensionalPosition.getWorld(world.getServer());
            if (!connectedWorld.isPresent())
                return LazyOptional.empty();

            ServerWorld csw = connectedWorld.get();

            BlockPos connectedPos = dimensionalPosition.getBlockPosition();
            if (connectedState.get().hasTileEntity()) {
                TileEntity connectedTile = csw.getBlockEntity(connectedPos);
                if (connectedTile != null)
                    return connectedTile.getCapability(cap, twt.getTunnelSide().getOpposite());
            }

            return LazyOptional.empty();
        }

        return LazyOptional.empty();
    }


}
