package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.IRoomInformation;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.InstancedTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelInstance;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelTeardownHandler;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TunnelWallEntity extends BlockEntity {

    private GlobalPos connectedMachine;
    private TunnelDefinition tunnelType;

    private LazyOptional<IRoomInformation> ROOM = LazyOptional.empty();

    @Nullable
    private TunnelInstance tunnel;

    public TunnelWallEntity(BlockPos pos, BlockState state) {
        super(Tunnels.TUNNEL_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);

        try {
            // 4.3 and above
            final var baseData = BaseTunnelWallData.CODEC.parse(NbtOps.INSTANCE, nbt)
                    .getOrThrow(true, CompactMachines.LOGGER::fatal);

            this.connectedMachine = baseData.connection();
            this.tunnelType = baseData.tunnel();
        } catch (Exception e) {
            this.tunnelType = Tunnels.UNKNOWN.get();
            this.connectedMachine = null;
        }

        try {
            if (tunnelType instanceof InstancedTunnel it)
                this.tunnel = it.newInstance(worldPosition, getTunnelSide());

            if (tunnel instanceof INBTSerializable persist && nbt.contains("tunnel_data")) {
                var data = nbt.get("tunnel_data");
                persist.deserializeNBT(data);
            }
        } catch (Exception ex) {
            CompactMachines.LOGGER.error("Error loading tunnel persistent data at {}; this is likely a cross-mod issue!", worldPosition, ex);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel sl) {
            var chunk = level.getChunkAt(worldPosition);
            ROOM = chunk.getCapability(Capabilities.ROOM);

            // If tunnel type is unknown, remove the tunnel entirely
            // Null tunnel types here mean it's being loaded into the world
            if (this.tunnelType != null && tunnelType.equals(Tunnels.UNKNOWN.get())) {
                CompactMachines.LOGGER.warn("Removing unknown tunnel type at {}", worldPosition.toShortString());
                sl.setBlock(worldPosition, Registration.BLOCK_SOLID_WALL.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        final var baseData = new BaseTunnelWallData(connectedMachine, tunnelType.getRegistryName());
        final var baseNbt = (CompoundTag) BaseTunnelWallData.CODEC.encodeStart(NbtOps.INSTANCE, baseData)
                .getOrThrow(false, CompactMachines.LOGGER::error);

        compound.merge(baseNbt);

        if (tunnel instanceof INBTSerializable persist) {
            var data = persist.serializeNBT();
            compound.put("tunnel_data", data);
        }
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() {
        final var baseData = new BaseTunnelWallData(connectedMachine, tunnelType.getRegistryName());
        return (CompoundTag) BaseTunnelWallData.CODEC.encodeStart(NbtOps.INSTANCE, baseData)
                .getOrThrow(false, CompactMachines.LOGGER::error);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        final var baseData = BaseTunnelWallData.CODEC.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(true, CompactMachines.LOGGER::fatal);

        this.connectedMachine = baseData.connection();
        this.tunnelType = baseData.tunnel();
        setChanged();
    }

    @Nonnull
    public <T> LazyOptional<T> getTunnelCapability(@Nonnull Capability<T> cap, @Nullable Direction outerSide) {
        if (level == null || level.isClientSide)
            return LazyOptional.empty();

        if (outerSide != null && outerSide != getConnectedSide())
            return LazyOptional.empty();

        if (tunnelType instanceof CapabilityTunnel c) {
            return c.getCapability(cap, tunnel);
        }

        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (level == null || level.isClientSide)
            return super.getCapability(cap, side);

        if (side != null && side != getTunnelSide())
            return super.getCapability(cap, side);

        if (tunnelType instanceof CapabilityTunnel c) {
            return c.getCapability(cap, tunnel);
        }

        return super.getCapability(cap, side);
    }

    public GlobalPos getConnectedMachine() {
        return this.connectedMachine;
    }

    public GlobalPos getConnectedPosition() {
        if(this.connectedMachine == null)
            return null;

        return GlobalPos.of(connectedMachine.dimension(), connectedMachine.pos().relative(getConnectedSide()));
    }

    /**
     * Gets the side the tunnel is placed on (the wall inside the machine)
     */
    public Direction getTunnelSide() {
        BlockState state = getBlockState();
        return state.getValue(TunnelWallBlock.TUNNEL_SIDE);
    }

    /**
     * Gets the side the tunnel connects to externally (the machine side)
     */
    public Direction getConnectedSide() {
        BlockState blockState = getBlockState();
        return blockState.getValue(TunnelWallBlock.CONNECTED_SIDE);
    }

    public void setTunnelType(TunnelDefinition type) {
        if (type == tunnelType)
            return;

        if (level == null || level.isClientSide || !(level instanceof ServerLevel sl)) {
            tunnelType = type;
            return;
        }

        final var p = new TunnelPosition(sl, worldPosition, getTunnelSide());
        if (tunnelType instanceof TunnelTeardownHandler teardown) {
            teardown.onRemoved(p, tunnel);
        }

        this.tunnelType = type;
        if (type instanceof InstancedTunnel it)
            this.tunnel = it.newInstance(p.pos(), p.side());

        setChanged();
    }

    public TunnelDefinition getTunnelType() {
        return tunnelType;
    }

    /**
     * Server only. Changes where the tunnel is connected to.
     *
     * @param machine Machine to connect tunnel to.
     */
    public void setConnectedTo(GlobalPos machine, Direction side) {
        if (level == null || level.isClientSide) return;
        this.connectedMachine = machine;

        if(level instanceof ServerLevel sl) {
            final var graph = TunnelConnectionGraph.forRoom(sl, new ChunkPos(worldPosition));
            graph.rebind(worldPosition, machine, side);
        }
    }

    @Nullable
    public TunnelInstance getTunnel() {
        return tunnel;
    }

    public void setInstance(TunnelInstance newTunn) {
        this.tunnel = newTunn;
        setChanged();
    }

    public void disconnect() {
        if (level == null || level.isClientSide) {
            this.connectedMachine = null;
            return;
        }

        if(level instanceof ServerLevel compactDim && compactDim.dimension().equals(Registration.COMPACT_DIMENSION)) {
            final var tunnelData = TunnelConnectionGraph.forRoom(compactDim, new ChunkPos(worldPosition));
            tunnelData.unregister(worldPosition);

            this.connectedMachine = null;
            compactDim.setBlock(worldPosition, Registration.BLOCK_SOLID_WALL.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }
}
