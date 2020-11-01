package com.robotgryphon.compactmachines.block.tiles;

import com.robotgryphon.compactmachines.block.walls.TunnelWallBlock;
import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.reference.EnumTunnelType;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import com.robotgryphon.compactmachines.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.tunnels.TunnelHelper;
import com.robotgryphon.compactmachines.tunnels.api.ICapableTunnel;
import com.robotgryphon.compactmachines.util.CompactMachineUtil;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TunnelWallTile extends TileEntity {

    public TunnelWallTile() {
        super(Registrations.TUNNEL_WALL_TILE.get());
    }

    public Optional<CompactMachineRegistrationData> getMachineInfo() {
        if (this.world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) this.world;
            return CompactMachineUtil.getMachineInfoByInternalPosition(serverWorld, this.pos);
        }

        return Optional.empty();
    }

    /**
     * Gets the position outside the machine where this tunnel is connected to.
     *
     * @return
     */
    public Optional<DimensionalPosition> getConnectedPosition() {
        if (world == null || world.isRemote())
            return Optional.empty();

        ServerWorld serverWorld = (ServerWorld) world;

        Optional<CompactMachineRegistrationData> machineInfo = getMachineInfo();
        if (!machineInfo.isPresent())
            return Optional.empty();

        Direction outsideDir = getTunnelSide();

        CompactMachineRegistrationData regData = machineInfo.get();

        // Is the machine placed in world? If not, do not return an outside position
        if (!regData.isPlacedInWorld())
            return Optional.empty();

        DimensionalPosition machinePosition = regData.getOutsidePosition(serverWorld);
        if (machinePosition != null) {
            Vector3d o = machinePosition.getPosition();
            BlockPos machineOutPos = new BlockPos(o.x, o.y, o.z);

            BlockPos connectedBlock = machineOutPos.offset(outsideDir);
            Vector3d connectedBlockVec = new Vector3d(connectedBlock.getX(), connectedBlock.getY(), connectedBlock.getZ());

            DimensionalPosition connectedPosition = new DimensionalPosition(machinePosition.getDimension(), connectedBlockVec);
            return Optional.of(connectedPosition);
        }

        return Optional.empty();
    }

    public Direction getTunnelSide() {
        BlockState blockState = getBlockState();
        return blockState.get(TunnelWallBlock.TUNNEL_SIDE);
    }

    public Optional<TunnelDefinition> getTunnelDefinition() {
        BlockState state = getBlockState();
        EnumTunnelType type = state.get(TunnelWallBlock.TUNNEL_TYPE);

        return TunnelHelper.getTunnelDefinitionFromType(type);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        Direction tunnelInDir = getTunnelSide();

        Optional<TunnelDefinition> tunnelDef = getTunnelDefinition();

        // If we don't have a definition for the tunnel, skip
        if (!tunnelDef.isPresent())
            return super.getCapability(cap, side);

        // loop through tunnel definition for capabilities
        TunnelDefinition definition = tunnelDef.get();
        if (definition instanceof ICapableTunnel) {
            if(!world.isRemote()) {
                ServerWorld sw = (ServerWorld) world;
                BlockState state = getBlockState();
                return ((ICapableTunnel) definition).getCapability(sw, state, pos, cap, side);
            }
        }

        return super.getCapability(cap, side);
    }
}
