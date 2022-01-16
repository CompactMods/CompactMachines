package dev.compactmods.machines.tunnel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.room.IRoomCapabilities;
import dev.compactmods.machines.api.tunnels.ITunnel;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.capability.ICapabilityTunnel;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.api.tunnels.lifecycle.IPersistentTunnelData;
import dev.compactmods.machines.api.tunnels.lifecycle.ITunnelTeardown;
import dev.compactmods.machines.api.tunnels.lifecycle.TeardownReason;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.MachineConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class TunnelWallEntity extends BlockEntity {

    private int connectedMachine;
    private TunnelDefinition tunnelType;

    private final LazyOptional<ITunnelConnection> conn;
    private LazyOptional<IMachineRoom> ROOM = LazyOptional.empty();
    private LazyOptional<IRoomCapabilities> CAPS = LazyOptional.empty();
    private TunnelMachineConnection connection;
    private ITunnel tunnel;

    public TunnelWallEntity(BlockPos pos, BlockState state) {
        super(Tunnels.TUNNEL_BLOCK_ENTITY.get(), pos, state);
        this.tunnelType = Tunnels.UNKNOWN.get();
        this.conn = LazyOptional.of(this::getConnection);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setBlockState(@NotNull BlockState newState) {
        super.setBlockState(newState);
        if (level != null && !level.isClientSide)
            this.connection = new TunnelMachineConnection(level.getServer(), this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        try {
            if (nbt.contains("machine")) {
                this.connectedMachine = nbt.getInt("machine");
            }

            if (nbt.contains("tunnel_type")) {
                ResourceLocation type = new ResourceLocation(nbt.getString("tunnel_type"));
                this.tunnelType = Tunnels.getDefinition(type);
                this.tunnel = tunnelType.newInstance(worldPosition, getTunnelSide());

                try {
                    if (tunnel instanceof IPersistentTunnelData persist && nbt.contains("tunnel_data")) {
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
    public void saveAdditional(CompoundTag compound) {
        compound.putString("tunnel_type", tunnelType.getRegistryName().toString());
        compound.putInt("machine", connectedMachine);

        if (tunnelType instanceof IPersistentTunnelData<?> persist) {
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
            this.connection = new TunnelMachineConnection(sl.getServer(), this);

            var chunk = level.getChunkAt(worldPosition);
            CAPS = chunk.getCapability(Capabilities.ROOM_CAPS);
            ROOM = chunk.getCapability(Capabilities.ROOM);

            // TODO
//            if (tunnelType instanceof ITunnelSetup setup) {
//                ROOM.ifPresent(room -> setup.setup(room, new TunnelPosition(sl, worldPosition, getTunnelSide()), this.connection));
//            }
        }
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (level == null || level.isClientSide)
            return super.getCapability(cap, side);

        if (side != getTunnelSide())
            return super.getCapability(cap, side);

        if (cap == Capabilities.TUNNEL_CONNECTION)
            return conn.cast();

        if (tunnelType instanceof ICapabilityTunnel c) {
            return c.getCapability(cap, tunnel);
        }

        return super.getCapability(cap, side);
    }

    /**
     * Server side only. Gets information about the connection to an outside point.
     */
    public @NotNull ITunnelConnection getConnection() {
        if (this.connection == null)
            this.connection = new TunnelMachineConnection(level.getServer(), this);

        return this.connection;
    }

    public IDimensionalPosition getConnectedPosition() {
        if (level == null || level.isClientSide) return null;

        // TODO - Needs massive clean up here
        final MinecraftServer server = level.getServer();
        if (server == null) return null;

        var machines = CompactMachineData.get(server);
        var conn = MachineConnections.get(server);

        if (machines == null || conn == null)
            return null;

        final Collection<Integer> connected = conn.graph.getMachinesFor(new ChunkPos(worldPosition));
        final Optional<Integer> first = connected.stream().findFirst();
        if (first.isEmpty())
            return null;

        int machine = first.get();
        return machines.getMachineLocation(machine)
                .map(dp -> dp.relative(getConnectedSide()))
                .orElse(null);
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

    public Optional<TunnelDefinition> getTunnelDefinition() {
        if (tunnelType == null)
            return Optional.empty();

        return Optional.of(tunnelType);
    }

    public void setTunnelType(TunnelDefinition type) throws Exception {
        if (type == tunnelType)
            return;

        if (level == null || level.isClientSide || !(level instanceof ServerLevel sl)) {
            tunnelType = type;
            return;
        }

        ITunnelConnection conn = getConnection();

        final LevelChunk chunk = level.getChunkAt(worldPosition);
        var roomData = chunk.getCapability(Capabilities.ROOM).orElseThrow(() -> {
            CompactMachines.LOGGER.fatal("Failed to get room data off a chunk. This is a bug, report it!");
            return new Exception("Missing chunk room data.");
        });

        var p = new TunnelPosition(sl, worldPosition, getTunnelSide());
        if (tunnelType instanceof ITunnelTeardown teardown) {
            teardown.teardown(tunnel, TeardownReason.REMOVED);
        }

        this.tunnelType = type;
        this.tunnel = type.newInstance(p.pos(), p.side());
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

        CompactMachineData data = CompactMachineData.get(level.getServer());
        if (data == null)
            return;

        data.getMachineLocation(machine).ifPresent(p -> {
            this.connection = new TunnelMachineConnection(level.getServer(), this);
            this.conn.invalidate();
        });
    }

    public ITunnel getTunnel() {
        return tunnel;
    }

    public void setTunnel(ITunnel newTunn) {
        this.tunnel = newTunn;
        setChanged();
    }
}
