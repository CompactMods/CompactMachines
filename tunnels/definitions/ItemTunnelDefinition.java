package dev.compactmods.machines.tunnels.definitions;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.EnumTunnelSide;
import dev.compactmods.machines.tunnels.TunnelHelper;
import dev.compactmods.machines.api.tunnels.IItemTunnel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.*;

public class ItemTunnelDefinition extends TunnelDefinition implements IItemTunnel, IForgeRegistryEntry<TunnelDefinition> {

    @Override
    public int getTunnelRingColor() {
        return new Color(205, 143, 36).getRGB();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getInternalCapability(ServerLevel compactWorld, BlockPos tunnelPos, @Nonnull Capability<T> cap, Direction side) {
        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.empty();

        BlockEntity te = compactWorld.getBlockEntity(tunnelPos);
        if (te instanceof TunnelWallTile) {
            TunnelWallTile twt = (TunnelWallTile) te;

            Optional<BlockState> connectedState = TunnelHelper.getConnectedState(twt, EnumTunnelSide.INSIDE);
            if (!connectedState.isPresent())
                return LazyOptional.empty();

            Optional<IDimensionalPosition> tunnelConnectedPosition = TunnelHelper.getTunnelConnectedPosition(twt, EnumTunnelSide.INSIDE);
            if (!tunnelConnectedPosition.isPresent())
                return LazyOptional.empty();

            Direction tunnelSide = twt.getTunnelSide();

            IDimensionalPosition connectedInsidePos = tunnelConnectedPosition.get();
            if (connectedState.get().hasTileEntity()) {
                BlockEntity connectedTile = compactWorld.getBlockEntity(connectedInsidePos.getBlockPosition());
                if (connectedTile != null) {
                    return connectedTile.getCapability(cap, tunnelSide);
                }
            }
        }

        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getExternalCapability(ServerLevel world, BlockPos tunnelPos, @Nonnull Capability<T> cap, @Nullable Direction side) {
        BlockEntity te = world.getBlockEntity(tunnelPos);

        if (cap != CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return LazyOptional.empty();

        if (te instanceof TunnelWallTile) {
            TunnelWallTile twt = (TunnelWallTile) te;

            Optional<BlockState> connectedState = TunnelHelper.getConnectedState(twt, EnumTunnelSide.OUTSIDE);
            if (!connectedState.isPresent())
                return LazyOptional.empty();

            // link to external block capability
            Optional<IDimensionalPosition> connectedPosition = twt.getConnectedPosition();
            if (!connectedPosition.isPresent())
                return LazyOptional.empty();

            IDimensionalPosition dimensionalPosition = connectedPosition.get();
            // CompactMachines.LOGGER.debug(String.format("[%s] %s %s", 0, dimensionalPosition.getDimension(), dimensionalPosition.getPosition()));

            Optional<ServerLevel> connectedWorld = dimensionalPosition.getWorld(world.getServer());
            if (!connectedWorld.isPresent())
                return LazyOptional.empty();

            ServerLevel csw = connectedWorld.get();

            BlockPos connectedPos = dimensionalPosition.getBlockPosition();
            if (connectedState.get().hasTileEntity()) {
                BlockEntity connectedTile = csw.getBlockEntity(connectedPos);
                if (connectedTile != null)
                    return connectedTile.getCapability(cap, twt.getTunnelSide().getOpposite());
            }

            return LazyOptional.empty();
        }

        return LazyOptional.empty();
    }

    @Override
    public Map<Capability<?>, LazyOptional<?>> rebuildCapabilityCache(ServerLevel compactLevel, BlockPos tunnelPos, BlockPos inside, @Nullable IDimensionalPosition external) {
        HashMap<Capability<?>, LazyOptional<?>> set = new HashMap<>();

        BlockState innerState = compactLevel.getBlockState(inside);
        if(!innerState.hasTileEntity())
            return Collections.emptyMap();

        BlockEntity innerTile = compactLevel.getBlockEntity(inside);
        final LazyOptional<IItemHandler> items = innerTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        if(items.isPresent())
            set.put(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, items);

//        final Optional<ServerWorld> connectedLevel = external.getWorld(compactLevel.getServer());
//        connectedLevel.ifPresent(externLevel -> {
//            final BlockPos externPos = external.getBlockPosition();
//            if (externLevel.isLoaded(externPos)) {
//                BlockState connectedState = externLevel.getBlockState(externPos);
//                if(connectedState.hasTileEntity()) {
//                    TileEntity connectedTile = externLevel.getBlockEntity(externPos);
//                    final LazyOptional<IItemHandler> items = connectedTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
//                    set.put(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, items);
//                }
//            } else {
//                // other world not loaded - we need to defer this somehow TODO
//                CompactMachines.LOGGER.debug("not loaded");
//            }
//        });

        return set;
    }
}
