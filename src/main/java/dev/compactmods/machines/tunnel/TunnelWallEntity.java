package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.InstancedTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelInstance;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelTeardownHandler;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.machine.data.CompactMachineData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TunnelWallEntity extends BlockEntity {

    private int connectedMachine;
    private TunnelDefinition tunnelType;

    private LazyOptional<IMachineRoom> ROOM = LazyOptional.empty();

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
            if (nbt.contains("machine")) {
                this.connectedMachine = nbt.getInt("machine");
            }

            if (nbt.contains("tunnel_type")) {
                ResourceLocation type = new ResourceLocation(nbt.getString("tunnel_type"));
                this.tunnelType = Tunnels.getDefinition(type);

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
        } catch (Exception e) {
            this.tunnelType = Tunnels.UNKNOWN.get();
            this.connectedMachine = -1;
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        if (tunnelType != null)
            compound.putString("tunnel_type", tunnelType.getRegistryName().toString());
        else
            compound.putString("tunnel_type", Tunnels.UNKNOWN.getId().toString());

        compound.putInt("machine", connectedMachine);

        if (tunnel instanceof INBTSerializable persist) {
            var data = persist.serializeNBT();
            compound.put("tunnel_data", data);
        }
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        nbt.putString("tunnel_type", tunnelType.getRegistryName().toString());
        nbt.putInt("machine", connectedMachine);
        return nbt;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains("tunnel_type")) {
            var id = new ResourceLocation(tag.getString("tunnel_type"));
            this.tunnelType = Tunnels.getDefinition(id);
        }

        if (tag.contains("machine")) {
            this.connectedMachine = tag.getInt("machine");
        }

        setChanged();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level instanceof ServerLevel sl) {
            var chunk = level.getChunkAt(worldPosition);
            ROOM = chunk.getCapability(Capabilities.ROOM);

            // If tunnel type is unknown, remove the tunnel entirely
            if (tunnelType != null && tunnelType.equals(Tunnels.UNKNOWN.get())) {
                CompactMachines.LOGGER.warn("Removing unknown tunnel type at {}", worldPosition.toShortString());
                sl.setBlock(worldPosition, Registration.BLOCK_SOLID_WALL.get().defaultBlockState(), Block.UPDATE_ALL);
            }
        }
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

    public IDimensionalPosition getConnectedPosition() {
        if (level == null || level.isClientSide) return null;

        final MinecraftServer server = level.getServer();
        if (server == null)
            return null;

        try {
            CompactMachineData machines = CompactMachineData.get(server);

            return machines.getMachineLocation(this.connectedMachine)
                    .map(dp -> dp.relative(getConnectedSide()))
                    .orElse(null);

        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
            return null;
        }
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
        if(type instanceof InstancedTunnel it)
            this.tunnel = it.newInstance(p.pos(), p.side());

        setChanged();
    }

    public TunnelDefinition getTunnelType() {
        return tunnelType;
    }

    /**
     * Server only. Changes where the tunnel is connected to.
     *
     * @param machine Machine ID to connect tunnel to.
     */
    public void setConnectedTo(int machine) {
        if (level == null || level.isClientSide) return;

        CompactMachineData data;
        try {
            data = CompactMachineData.get(level.getServer());
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
            return;
        }

        data.getMachineLocation(machine).ifPresent(p -> {
            this.connectedMachine = machine;
        });
    }

    @Nullable
    public TunnelInstance getTunnel() {
        return tunnel;
    }

    public void setInstance(TunnelInstance newTunn) {
        this.tunnel = newTunn;
        setChanged();
    }

    public int getMachine() {
        return connectedMachine;
    }

    public void disconnect() {

        if (level == null || level.isClientSide) {
            this.connectedMachine = -1;
            return;
        }

        CompactMachineData data;
        try {
            data = CompactMachineData.get(level.getServer());
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
            return;
        }

        data.remove(this.connectedMachine);
        this.connectedMachine = -1;
        this.setChanged();
    }
}
