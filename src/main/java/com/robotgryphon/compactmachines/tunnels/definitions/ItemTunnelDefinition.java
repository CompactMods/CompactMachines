package com.robotgryphon.compactmachines.tunnels.definitions;

import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import com.robotgryphon.compactmachines.tunnels.EnumTunnelSide;
import com.robotgryphon.compactmachines.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.tunnels.TunnelHelper;
import com.robotgryphon.compactmachines.tunnels.api.IItemTunnel;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
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

    public ItemTunnelDefinition(Item item) {
        super(item);
    }

    @Override
    public int getTunnelRingColor() {
        return new Color(205, 143, 36).getRGB();
    }

    @Override
    public int getTunnelIndicatorColor() {
        return TunnelDefinition.NO_INDICATOR_COLOR;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(ServerWorld world, BlockState state, BlockPos pos, @Nonnull Capability<T> cap, @Nullable Direction side) {
        TileEntity te = world.getTileEntity(pos);

        if(cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.empty();

        if (te instanceof TunnelWallTile) {
            TunnelWallTile twt = (TunnelWallTile) te;

            Optional<BlockState> connectedState = TunnelHelper.getConnectedState(world, twt, EnumTunnelSide.OUTSIDE);
            if (!connectedState.isPresent())
                return LazyOptional.empty();

            // link to external block capability
            Optional<DimensionalPosition> connectedPosition = twt.getConnectedPosition();
            if (!connectedPosition.isPresent())
                return LazyOptional.empty();

            DimensionalPosition dimensionalPosition = connectedPosition.get();
            // CompactMachines.LOGGER.debug(String.format("[%s] %s %s", 0, dimensionalPosition.getDimension(), dimensionalPosition.getPosition()));

            Optional<ServerWorld> connectedWorld = dimensionalPosition.getWorld(world);
            if (!connectedWorld.isPresent())
                return LazyOptional.empty();

            ServerWorld csw = connectedWorld.get();

            BlockPos connectedPos = dimensionalPosition.getBlockPosition();
            if(connectedState.get().hasTileEntity()) {
                TileEntity connectedTile = csw.getTileEntity(connectedPos);

                return connectedTile.getCapability(cap, side);
            }

            return LazyOptional.empty();
        }

        return LazyOptional.empty();
    }


}
