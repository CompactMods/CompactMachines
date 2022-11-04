package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.tunnels.ITunnelHolder;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.InstancedTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelInstance;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelTeardownHandler;
import dev.compactmods.machines.codec.CodecExtensions;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.TunnelNode;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class TunnelWallEntity extends BlockEntity implements ITunnelHolder {

    private static final String NBT_LEGACY_MACHINE_KEY = "machine";

    private LevelBlockPosition connectedMachine;
    private TunnelDefinition tunnelType;

    @Nullable
    private TunnelInstance tunnel;

    private WeakReference<TunnelNode> node;

    public TunnelWallEntity(BlockPos pos, BlockState state) {
        super(Tunnels.TUNNEL_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);

        final var baseData = BaseTunnelWallData.CODEC.parse(NbtOps.INSTANCE, nbt)
                .getOrThrow(true, CompactMachines.LOGGER::fatal);

        this.connectedMachine = baseData.connection();
        this.tunnelType = baseData.tunnel();

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
            // If tunnel type is unknown, remove the tunnel entirely
            // Null tunnel types here mean it's being loaded into the world
            if (this.tunnelType != null && tunnelType.equals(Tunnels.UNKNOWN.get())) {
                CompactMachines.LOGGER.warn("Removing unknown tunnel type at {}", worldPosition.toShortString());
                sl.setBlock(worldPosition, Walls.BLOCK_SOLID_WALL.get().defaultBlockState(), Block.UPDATE_ALL);
            } else {
                // todo Load tunnel data
                node = new WeakReference<>(null);
            }
        }
    }

    @Override
    public void saveAdditional(@Nonnull CompoundTag compound) {
        CodecExtensions.writeIntoTag(BaseTunnelWallData.CODEC,
                new BaseTunnelWallData(connectedMachine, Tunnels.getRegistryId(tunnelType)),
                compound);

        if (tunnel instanceof INBTSerializable persist) {
            var data = persist.serializeNBT();
            compound.put("tunnel_data", data);
        }
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        return CodecExtensions.writeIntoTag(BaseTunnelWallData.CODEC,
                new BaseTunnelWallData(connectedMachine, Tunnels.getRegistryId(tunnelType)),
                nbt);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        if (tag.contains(BaseTunnelWallData.KEY_TUNNEL_TYPE)) {
            var id = new ResourceLocation(tag.getString(BaseTunnelWallData.KEY_TUNNEL_TYPE));
            this.tunnelType = Tunnels.getDefinition(id);
        }

        if (tag.contains(BaseTunnelWallData.KEY_CONNECTION)) {
            this.connectedMachine = LevelBlockPosition.fromNBT(tag.getCompound(BaseTunnelWallData.KEY_CONNECTION));
        }

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

    @NotNull
    public IDimensionalBlockPosition getConnectedPosition() {
        return this.connectedMachine;
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

        final var p = new TunnelPosition(worldPosition, getTunnelSide(), getConnectedSide());
        if (tunnelType instanceof TunnelTeardownHandler teardown) {
            teardown.onRemoved(sl.getServer(), p, tunnel);
        }

        this.tunnelType = type;
        if (type instanceof InstancedTunnel it)
            this.tunnel = it.newInstance(p.pos(), p.wallSide());

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
    public void setConnectedTo(IDimensionalBlockPosition machine, Direction side) {
        if (level == null || level.isClientSide) return;
        this.connectedMachine = new LevelBlockPosition(machine);
        if (level instanceof ServerLevel sl) {

            // TODO - Weak references to room data so we don't have to do this
            final var roomProvider = CompactRoomProvider.instance(sl);
            roomProvider.findByChunk(new ChunkPos(this.worldPosition)).ifPresent(room -> {
                final var graph = TunnelConnectionGraph.forRoom(sl, room.code());
                graph.rebind(worldPosition, machine, side);
            });
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <U extends TunnelInstance> U getTunnel() {
        try {
            return (U) tunnel;
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    public void setInstance(TunnelInstance newTunn) {
        this.tunnel = newTunn;
        setChanged();
    }

    public TunnelPosition getTunnelPosition() {
        return new TunnelPosition(worldPosition, getTunnelSide(), getConnectedSide());
    }
}
