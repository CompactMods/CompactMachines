package com.robotgryphon.compactmachines.block.tiles;

import com.robotgryphon.compactmachines.block.walls.TunnelWallBlock;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.network.NetworkHandler;
import com.robotgryphon.compactmachines.network.TunnelAddedPacket;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import com.robotgryphon.compactmachines.api.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.api.tunnels.ICapableTunnel;
import com.robotgryphon.compactmachines.util.CompactMachineUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class TunnelWallTile extends TileEntity {

    private ResourceLocation tunnelType;

    public TunnelWallTile() {
        super(Registration.TUNNEL_WALL_TILE.get());
    }

    public Optional<CompactMachineRegistrationData> getMachineInfo() {
        if (this.world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) this.world;
            return CompactMachineUtil.getMachineInfoByInternalPosition(serverWorld, this.pos);
        }

        return Optional.empty();
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        if(nbt.contains("tunnel_type")) {
            ResourceLocation type = new ResourceLocation(nbt.getString("tunnel_type"));
            this.tunnelType = type;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound = super.write(compound);
        compound.putString("tunnel_type", tunnelType.toString());
        return compound;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = super.getUpdateTag();
        nbt.putString("tunnel_type", tunnelType.toString());

        return nbt;
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        super.handleUpdateTag(state, tag);

        if(tag.contains("tunnel_type")) {
            this.tunnelType = new ResourceLocation(tag.getString("tunnel_type"));
        }
    }

    public Optional<? extends IWorldReader> getConnectedWorld() {
        if (world == null || world.isRemote())
            return Optional.empty();

        ServerWorld serverWorld = (ServerWorld) world;

        Optional<CompactMachineRegistrationData> machineInfo = getMachineInfo();
        if (!machineInfo.isPresent())
            return Optional.empty();

        Direction outsideDir = getConnectedSide();

        CompactMachineRegistrationData regData = machineInfo.get();

        // Is the machine placed in world? If not, do not return an outside position
        if (!regData.isPlacedInWorld())
            return Optional.empty();

        DimensionalPosition machinePosition = regData.getOutsidePosition(serverWorld.getServer());
        if (machinePosition != null) {
            return machinePosition.getWorld(serverWorld.getServer());
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

        Direction outsideDir = getConnectedSide();

        CompactMachineRegistrationData regData = machineInfo.get();

        // Is the machine placed in world? If not, do not return an outside position
        if (!regData.isPlacedInWorld())
            return Optional.empty();

        DimensionalPosition machinePosition = regData.getOutsidePosition(serverWorld.getServer());
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

    /**
     * Gets the side the tunnel is placed on (the wall inside the machine)
     * @return
     */
    public Direction getTunnelSide() {
        BlockState state = getBlockState();
        return state.get(TunnelWallBlock.TUNNEL_SIDE);
    }

    /**
     * Gets the side the tunnel connects to externally (the machine side)
     * @return
     */
    public Direction getConnectedSide() {
        BlockState blockState = getBlockState();
        return blockState.get(TunnelWallBlock.CONNECTED_SIDE);
    }

    public Optional<ResourceLocation> getTunnelDefinitionId() {
        return Optional.ofNullable(this.tunnelType);
    }

    public Optional<TunnelDefinition> getTunnelDefinition() {
        if(tunnelType == null)
            return Optional.empty();

        TunnelDefinition definition = GameRegistry
                .findRegistry(TunnelDefinition.class)
                .getValue(tunnelType);

        return Optional.ofNullable(definition);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        Direction tunnelInDir = getConnectedSide();

        Optional<TunnelDefinition> tunnelDef = getTunnelDefinition();

        // If we don't have a definition for the tunnel, skip
        if (!tunnelDef.isPresent())
            return super.getCapability(cap, side);

        // loop through tunnel definition for capabilities
        TunnelDefinition definition = tunnelDef.get();
        if (definition instanceof ICapableTunnel) {
            if(!world.isRemote()) {
                ServerWorld sw = (ServerWorld) world;
                return ((ICapableTunnel) definition).getExternalCapability(sw, pos, cap, side);
            }
        }

        return super.getCapability(cap, side);
    }

    public void setTunnelType(ResourceLocation registryName) {
        this.tunnelType = registryName;

        if(world != null && !world.isRemote()) {
            markDirty();

            TunnelAddedPacket pkt = new TunnelAddedPacket(pos, registryName);

            Chunk chunkAt = world.getChunkAt(pos);
            NetworkHandler.MAIN_CHANNEL
                    .send(PacketDistributor.TRACKING_CHUNK.with(() -> chunkAt), pkt);
        }
    }
}
