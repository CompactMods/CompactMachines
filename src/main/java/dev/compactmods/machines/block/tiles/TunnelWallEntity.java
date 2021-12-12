package dev.compactmods.machines.block.tiles;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.location.SidedPosition;
import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.room.IRoomCapabilities;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.capability.ITunnelCapabilitySetup;
import dev.compactmods.machines.api.tunnels.capability.ITunnelCapabilityTeardown;
import dev.compactmods.machines.api.tunnels.connection.IMachineTunnels;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.block.walls.TunnelWallBlock;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.CompactRoomData;
import dev.compactmods.machines.data.persistent.MachineConnections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class TunnelWallEntity extends BlockEntity {

    private int connectedMachine;
    private TunnelDefinition tunnelType;

    private final HashMap<SidedPosition, Capability<?>> capabilities;
    private final HashMap<SidedPosition, LazyOptional<?>> capabilityCache;

    public TunnelWallEntity(BlockPos pos, BlockState state) {
        super(Tunnels.TUNNEL_BLOCK_ENTITY.get(), pos, state);
        this.capabilities = new HashMap<>();
        this.capabilityCache = new HashMap<>();
        this.tunnelType = Tunnels.UNKNOWN.get();
    }

    public TunnelWallEntity(BlockPos pos, BlockState state, TunnelDefinition def) {
        super(Tunnels.TUNNEL_BLOCK_ENTITY.get(), pos, state);
        this.capabilities = new HashMap<>();
        this.capabilityCache = new HashMap<>();
        this.tunnelType = def;
    }

    @Override
    public void load(CompoundTag nbt) {
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

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(level.isClientSide)
            return super.getCapability(cap, side);

        if (side != getTunnelSide())
            return super.getCapability(cap, side);

        SidedPosition p = new SidedPosition(side, worldPosition);
        if (capabilities.containsKey(p)) {
            var c = capabilities.get(p);
            if(c != cap)
                return super.getCapability(cap, side);

            if (capabilityCache.containsKey(p))
                return capabilityCache.get(p).cast();

            CompactMachines.LOGGER.debug("No capability registered yet: {} / {}", worldPosition, side);
            return super.getCapability(cap, side);
        }

        return super.getCapability(cap, side);
    }

    public IDimensionalPosition getConnectedPosition() {
        if(level.isClientSide) return null;

        // TODO - Needs massive clean up here
        var machines = CompactMachineData.get(level.getServer());
        var conn = MachineConnections.get(level.getServer());
        var rooms = CompactRoomData.get(level.getServer());

        final Collection<Integer> connected = conn.graph.getMachinesFor(new ChunkPos(worldPosition));
        final Optional<Integer> first = connected.stream().findFirst();
        if(first.isEmpty())
            return null;

        int machine = first.get();
        return machines.getMachineLocation(machine).orElse(null);
    }

    private Optional<Integer> tryFindExternalMachineByChunkPos(MachineConnections connections) {
        ChunkPos thisMachineChunk = new ChunkPos(worldPosition);
        Collection<Integer> externalMachineIDs = connections.graph.getMachinesFor(thisMachineChunk);

        // This shouldn't happen - there should always be at least one machine attached externally
        // If this DOES happen, it's probably a migration failure or the block was destroyed without notification
        if (externalMachineIDs.isEmpty()) {
            CompactMachines.LOGGER.warn("Warning: Tunnel applied to a machine but no external machine data found.");
            CompactMachines.LOGGER.warn("Please validate the tunnel at: " + worldPosition.toShortString());
            return Optional.empty();
        }

        int first = externalMachineIDs.stream().findFirst().orElse(-1);
        // sanity - makes compiler happier, we already did a check above for empty state
        if (first == -1) return Optional.empty();

        // In theory, we can re-attach the tunnel to the first found external machine, if the saved data
        // does not actually contain an attached external id
        return Optional.of(first);
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

    public void setTunnelType(TunnelDefinition type) {
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

        IRoomCapabilities caps = new IRoomCapabilities() {
            @Override
            public <T> void addCapability(Capability<T> capability, T instance, Direction side) {
                SidedPosition change = new SidedPosition(side, worldPosition);
                capabilities.put(change, capability);
                capabilityCache.put(change, LazyOptional.of(() -> instance));
            }

            @Override
            public <T> void removeCapability(Capability<T> capability, Direction side) {
                SidedPosition change = new SidedPosition(side, worldPosition);
                capabilities.remove(change);
                capabilityCache.remove(change);
            }
        };

        IMachineRoom room = new IMachineRoom() {
            @NotNull
            @Override
            public ChunkPos getChunk() {
                return new ChunkPos(worldPosition);
            }

            @NotNull
            @Override
            public ServerLevel getLevel() {
                return (ServerLevel) level;
            }

            @NotNull
            @Override
            public IMachineTunnels getTunnels() {
                return new IMachineTunnels() {
                    @Override
                    public Stream<ITunnelConnection> getTunnels() {
                        return Stream.empty();
                    }
                };
            }

            @NotNull
            @Override
            public IRoomCapabilities getCapabilities() {
                return caps;
            }
        };

        if (tunnelType instanceof ITunnelCapabilityTeardown teardown) {
            teardown.teardownCapabilities(room, conn);
        }

        this.tunnelType = type;
        if (tunnelType instanceof ITunnelCapabilitySetup setup) {
            setup.setupCapabilities(room, conn);
        }

        setChanged();
    }
}
