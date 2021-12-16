package dev.compactmods.machines.block.tiles;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Optional;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.room.IRoomCapabilities;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.capability.ITunnelCapabilitySetup;
import dev.compactmods.machines.api.tunnels.capability.ITunnelCapabilityTeardown;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.block.walls.TunnelWallBlock;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.CompactRoomData;
import dev.compactmods.machines.data.persistent.MachineConnections;
import dev.compactmods.machines.tunnel.TunnelMachineConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class TunnelWallEntity extends BlockEntity {

    private int connectedMachine;
    private TunnelDefinition tunnelType;

    private LazyOptional<IMachineRoom> ROOM = LazyOptional.empty();
    private static LazyOptional<IRoomCapabilities> CAPS = LazyOptional.empty();
    private TunnelMachineConnection connection;

    public TunnelWallEntity(BlockPos pos, BlockState state) {
        super(Tunnels.TUNNEL_BLOCK_ENTITY.get(), pos, state);
        this.tunnelType = Tunnels.UNKNOWN.get();
    }

    @Override
    public void load(@NotNull CompoundTag nbt) {
        super.load(nbt);

        try {
            if (nbt.contains("tunnel_type")) {
                ResourceLocation type = new ResourceLocation(nbt.getString("tunnel_type"));
                this.tunnelType = Tunnels.getDefinition(type);
            }

            if (nbt.contains("machine")) {
                this.connectedMachine = nbt.getInt("machine");
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

        var chunk = level.getChunkAt(worldPosition);
        CAPS = chunk.getCapability(Capabilities.ROOM_CAPS);
        ROOM = chunk.getCapability(Capabilities.ROOM);

        CAPS.ifPresent(caps -> {
            if(tunnelType instanceof ITunnelCapabilitySetup setup) {
                // todo ROOM.ifPresent(room ->setup.setupCapabilities(room, );
            }
        });
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (level == null || level.isClientSide)
            return super.getCapability(cap, side);

        if (side != getTunnelSide())
            return super.getCapability(cap, side);

        final var chunk = level.getChunkAt(worldPosition);
        if (cap == Capabilities.ROOM)
            return chunk.getCapability(Capabilities.ROOM).cast();

        if (cap == Capabilities.ROOM_CAPS)
               return chunk.getCapability(Capabilities.ROOM_CAPS).cast();

        return chunk.getCapability(Capabilities.ROOM).lazyMap(caps -> {
            return caps.getCapabilities().getCapability(cap, side);
        }).orElseGet(() -> super.getCapability(cap, side));
    }

    public ITunnelConnection getConnection() {
        if(this.connection == null)
            this.connection = new TunnelMachineConnection(level, this);

        return this.connection;
    }

    public IDimensionalPosition getConnectedPosition() {
        if (level == null || level.isClientSide) return null;

        // TODO - Needs massive clean up here
        final MinecraftServer server = level.getServer();
        if (server == null) return null;

        var machines = CompactMachineData.get(server);
        var conn = MachineConnections.get(server);
        var rooms = CompactRoomData.get(server);

        final Collection<Integer> connected = conn.graph.getMachinesFor(new ChunkPos(worldPosition));
        final Optional<Integer> first = connected.stream().findFirst();
        if (first.isEmpty())
            return null;

        int machine = first.get();
        return machines.getMachineLocation(machine).orElse(null);
    }

    /**
     * Gets the side the tunnel is placed on (the wall inside the machine)
     *
     * @return
     */
    public Direction getTunnelSide() {
        BlockState state = getBlockState();
        return state.getValue(TunnelWallBlock.TUNNEL_SIDE);
    }

    /**
     * Gets the side the tunnel connects to externally (the machine side)
     *
     * @return
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

        if (level.isClientSide) {
            tunnelType = type;
            return;
        }

        ITunnelConnection conn = new ITunnelConnection() {
            @NotNull
            @Override
            public TunnelDefinition tunnelType() {
                return tunnelType;
            }

            @NotNull
            @Override
            public IDimensionalPosition position() {
                return getConnectedPosition();
            }

            @NotNull
            @Override
            public BlockState state() {
                var pos = getConnectedPosition();
                return pos.getWorld(level.getServer())
                        .map(l -> l.getBlockState(pos.getBlockPosition()))
                        .orElse(Blocks.AIR.defaultBlockState());
            }

            @NotNull
            @Override
            public Direction side() {
                return getTunnelSide();
            }
        };

        final LevelChunk chunk = level.getChunkAt(worldPosition);
        var roomData = chunk.getCapability(Capabilities.ROOM).orElseThrow(() -> {
            CompactMachines.LOGGER.fatal("Failed to get room data off a chunk. This is a bug, report it!");
            return new Exception("Missing chunk room data.");
        });

        if (tunnelType instanceof ITunnelCapabilityTeardown teardown) {
            teardown.teardownCapabilities(roomData, conn);
        }

        this.tunnelType = type;
        if (tunnelType instanceof ITunnelCapabilitySetup setup) {
            setup.setupCapabilities(roomData, conn);
        }

        setChanged();
    }

    public TunnelDefinition getTunnelType() {
        return tunnelType;
    }

    /**
     * Server only. Changes where the tunnel is connected to.
     * @param machine
     */
    public void setConnectedTo(int machine) {
        if(level == null || level.isClientSide) return;
        
        CompactMachineData data = CompactMachineData.get(level.getServer());
        if(data == null)
            return;

        data.getMachineLocation(machine).ifPresent(this.connection::setLocation);
    }
}
