package dev.compactmods.machines.forge.tunnel;

import dev.compactmods.machines.api.tunnels.ITunnelHolder;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.InstancedTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelInstance;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelTeardownHandler;
import dev.compactmods.machines.api.tunnels.lifecycle.removal.ITunnelRemoveEventListener;
import dev.compactmods.machines.api.tunnels.lifecycle.removal.ITunnelRemoveReason;
import dev.compactmods.machines.codec.CodecExtensions;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.wall.Walls;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.tunnel.BaseTunnelWallData;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.tunnel.graph.node.TunnelNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class TunnelWallEntity extends BlockEntity implements ITunnelHolder {

    private static final String NBT_LEGACY_MACHINE_KEY = "machine";

    private GlobalPos connectedMachine;

    private ResourceKey<TunnelDefinition> tunnelTypeKey;
    private TunnelDefinition tunnelType;

    @Nullable
    private TunnelInstance tunnel;

    private WeakReference<TunnelNode> node;

    public TunnelWallEntity(BlockPos pos, BlockState state) {
        super(Tunnels.TUNNEL_BLOCK_ENTITY.get(), pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        final var baseData = BaseTunnelWallData.CODEC.parse(NbtOps.INSTANCE, nbt)
                .getOrThrow(true, CompactMachines.LOGGER::fatal);

        this.connectedMachine = baseData.connection();
        this.tunnelTypeKey = ResourceKey.create(TunnelDefinition.REGISTRY_KEY, baseData.tunnelType());
        this.tunnelType = Tunnels.getDefinition(this.tunnelTypeKey);

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
    public void saveAdditional(@NotNull CompoundTag compound) {
        CodecExtensions.writeIntoTag(BaseTunnelWallData.CODEC,
                new BaseTunnelWallData(connectedMachine, tunnelTypeKey.location()),
                compound);

        if (tunnel instanceof INBTSerializable persist) {
            var data = persist.serializeNBT();
            compound.put("tunnel_data", data);
        }
    }

    @Override
    @NotNull
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        return CodecExtensions.writeIntoTag(BaseTunnelWallData.CODEC,
                new BaseTunnelWallData(connectedMachine, tunnelTypeKey.location()),
                nbt);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        final var data = BaseTunnelWallData.CODEC.parse(NbtOps.INSTANCE, tag)
                .getOrThrow(false, CompactMachines.LOGGER::error);

        this.tunnelTypeKey = ResourceKey.create(TunnelDefinition.REGISTRY_KEY, data.tunnelType());
        this.tunnelType = Tunnels.getDefinition(tunnelTypeKey);
        this.connectedMachine = data.connection();

        setChanged();
    }

    @NotNull
    public <T> LazyOptional<T> getTunnelCapability(@NotNull Capability<T> cap, @Nullable Direction outerSide) {
        if (level == null || level.isClientSide)
            return LazyOptional.empty();

        if (outerSide != null && outerSide != getConnectedSide())
            return LazyOptional.empty();

        if (tunnelType instanceof CapabilityTunnel c) {
            return c.getCapability(cap, tunnel);
        }

        return LazyOptional.empty();
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
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
    public GlobalPos connectedMachine() {
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void serverPreRemoval(@Nullable ITunnelRemoveReason reason) {
        final var p = new TunnelPosition(worldPosition, getTunnelSide(), getConnectedSide());
        if (tunnelType instanceof TunnelTeardownHandler teardown) {
            //noinspection removal
            teardown.onRemoved(p, tunnel);
        }

        if (tunnelType instanceof ITunnelRemoveEventListener removeListener) {
            final var handler = removeListener.createBeforeRemoveHandler(tunnel);
            if (handler != null)
                handler.beforeRemove(level.getServer(), p, reason);
        }
    }

    private void serverPostRemoval(@Nullable ITunnelRemoveReason reason) {
        final var p = new TunnelPosition(worldPosition, getTunnelSide(), getConnectedSide());
        if (this.tunnelType instanceof InstancedTunnel it)
            this.tunnel = it.newInstance(p.pos(), p.wallSide());

        if (this.tunnelType instanceof ITunnelRemoveEventListener removeListener) {
            removeListener.createAfterRemoveHandler(tunnel)
                    .afterRemove(level.getServer(), p, reason);
        }
    }

    // TODO : Fix tunnel removal and placement coloration

    private void setTunnelTypeInternal(ResourceKey<TunnelDefinition> key, TunnelDefinition definition) {
        if (level == null || level.isClientSide || !(level instanceof ServerLevel)) {
            this.tunnelTypeKey = key;
            this.tunnelType = definition;
            return;
        }

        // TODO Clean all this up properly with a real removal reason
        serverPreRemoval(null);

        this.tunnelTypeKey = key;
        this.tunnelType = definition;

        serverPostRemoval(null);
        setChanged();
    }

    @Override
    public void setTunnelType(ResourceKey<TunnelDefinition> type) {
        if(type == null) {
            CompactMachines.LOGGER.warn("Removing tunnel at {} due to it being set null", worldPosition.toShortString());
            level.setBlock(worldPosition, Walls.BLOCK_SOLID_WALL.get().defaultBlockState(), Block.UPDATE_ALL);
            return;
        }

        if (type.equals(this.tunnelTypeKey))
            return;

        final var def = Tunnels.getDefinition(type);
        setTunnelTypeInternal(type, def);
    }

    @Override
    @Deprecated(forRemoval = true, since = "5.2.0")
    @SuppressWarnings("removal")
    public void setTunnelType(TunnelDefinition definition) {
        final var key = Tunnels.getRegistryKey(definition);
        if (this.tunnelTypeKey.equals(key))
            return;

        setTunnelTypeInternal(key, definition);
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
