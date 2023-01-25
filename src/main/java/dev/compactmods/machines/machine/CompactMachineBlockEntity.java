package dev.compactmods.machines.machine;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.codec.NbtListCollector;
import dev.compactmods.machines.api.machine.MachineNbt;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.machine.graph.legacy.LegacyMachineConnections;
import dev.compactmods.machines.room.graph.CompactMachineRoomNode;
import dev.compactmods.machines.tunnel.TunnelWallEntity;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CompactMachineBlockEntity extends BlockEntity {
    private static final String ROOM_NBT = "room_pos";
    private static final String LEGACY_MACH_ID = "machine_id";

    private static final String PLAYERS_NBT = "players_inside";
    private static final String TUNNEL_POSITIONS_NBT = "has_tunnels";

    public long nextSpawnTick = 0;

    protected UUID owner;
    protected String schema;
    protected boolean locked = false;
    private ChunkPos roomChunk;
    private int legacyMachineId = -1;

    private WeakReference<CompactMachineNode> graphNode;
    private WeakReference<CompactMachineRoomNode> roomNode;
    private final HashSet<UUID> playersInside = new HashSet<>();
    private final HashSet<GlobalPos> connectedTunnels = new HashSet<>();

    public CompactMachineBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.MACHINE_TILE_ENTITY.get(), pos, state);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (level instanceof ServerLevel sl) {
            return getConnectedRoom().map(roomId -> {
                try {
                    final var serv = sl.getServer();
                    final var compactDim = serv.getLevel(Registration.COMPACT_DIMENSION);

                    final var graph = TunnelConnectionGraph.forRoom(compactDim, roomId);

                    final var supportingTunnels = graph.getTunnelsSupporting(getLevelPosition(), side, cap);
                    final var firstSupported = supportingTunnels.findFirst();
                    if (firstSupported.isEmpty())
                        return super.getCapability(cap, side);

                    final var compact = serv.getLevel(Registration.COMPACT_DIMENSION);
                    if (compact == null)
                        throw new MissingDimensionException();

                    if (compact.getBlockEntity(firstSupported.get()) instanceof TunnelWallEntity tunnel) {
                        return tunnel.getTunnelCapability(cap, side);
                    } else {
                        return super.getCapability(cap, side);
                    }
                } catch (MissingDimensionException e) {
                    CompactMachines.LOGGER.fatal(e);
                    return super.getCapability(cap, side);
                }
            }).orElse(super.getCapability(cap, side));
        }

        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (this.legacyMachineId != -1)
            this.updateLegacyData();

        this.syncConnectedRoom();
    }

    @Override
    public void load(@Nonnull CompoundTag nbt) {
        super.load(nbt);

        // TODO customName = nbt.getString("CustomName");
        if (nbt.contains(MachineNbt.OWNER)) {
            owner = nbt.getUUID(MachineNbt.OWNER);
        } else {
            owner = null;
        }

        if (nbt.contains(LEGACY_MACH_ID)) {
            this.legacyMachineId = nbt.getInt(LEGACY_MACH_ID);
        }

        nextSpawnTick = nbt.getLong("spawntick");
        if (nbt.contains("schema")) {
            schema = nbt.getString("schema");
        } else {
            schema = null;
        }

        if (nbt.contains("locked")) {
            locked = nbt.getBoolean("locked");
        } else {
            locked = false;
        }
    }

    private void updateLegacyData() {
        if (level instanceof ServerLevel sl) {
            try {
                final var legacy = LegacyMachineConnections.get(sl.getServer());

                DimensionMachineGraph graph = DimensionMachineGraph.forDimension(sl);
                graph.addMachine(worldPosition);

                final ChunkPos oldRoom = legacy.getConnectedRoom(this.legacyMachineId);
                CompactMachines.LOGGER.info(CompactMachines.CONN_MARKER, "Rebinding machine {} ({}/{}) to room {}", legacyMachineId, worldPosition, level.dimension(), roomChunk);

                this.roomChunk = oldRoom;
                graph.connectMachineToRoom(worldPosition, roomChunk);
            } catch (MissingDimensionException e) {
                CompactMachines.LOGGER.fatal(CompactMachines.CONN_MARKER, "Could not load connection info from legacy data; machine at {} in dimension {} will be unmapped.", worldPosition, level.dimension());
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        // nbt.putString("CustomName", customName.getString());

        if (owner != null) {
            nbt.putUUID(MachineNbt.OWNER, this.owner);
        }

        nbt.putLong("spawntick", nextSpawnTick);
        if (schema != null) {
            nbt.putString("schema", schema);
        }

        nbt.putBoolean("locked", locked);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag data = super.getUpdateTag();

        if (level instanceof ServerLevel serverLevel) {
            final var compactDim = serverLevel.getServer().getLevel(Registration.COMPACT_DIMENSION);
            Objects.requireNonNull(compactDim);

            if (this.owner != null)
                data.putUUID("owner", this.owner);

            final var graph = DimensionMachineGraph.forDimension(serverLevel);
            var chunk = graph.getConnectedRoom(worldPosition);
            chunk.ifPresent(room -> {
                // write room position
                data.putIntArray(ROOM_NBT, new int[]{room.x, room.z});

                // players and tunnels
                final var playersInRoom = compactDim.getPlayers(player -> player.chunkPosition().equals(room));
                if (!playersInRoom.isEmpty()) {
                    final var playersNbt = playersInRoom.stream()
                            .map(ServerPlayer::getStringUUID)
                            .map(StringTag::valueOf)
                            .collect(NbtListCollector.toNbtList());

                    data.put(PLAYERS_NBT, playersNbt);
                }

                final var machineLoc = GlobalPos.of(serverLevel.dimension(), worldPosition);
                final var tunnels = TunnelConnectionGraph.forRoom(compactDim, room)
                        .getConnections(machineLoc)
                        .map(bp -> GlobalPos.of(compactDim.dimension(), bp))
                        .toList();

                final var tunnelPositions = GlobalPos.CODEC.listOf()
                        .encodeStart(NbtOps.INSTANCE, tunnels)
                        .getOrThrow(false, CompactMachines.LOGGER::error);

                data.put(TUNNEL_POSITIONS_NBT, tunnelPositions);
            });
        }

        return data;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);

        playersInside.clear();
        if (tag.contains(PLAYERS_NBT)) {
            final var pi = tag.getList(PLAYERS_NBT, Tag.TAG_INT_ARRAY)
                    .stream()
                    .map(Tag::getAsString)
                    .map(UUID::fromString)
                    .collect(Collectors.toUnmodifiableSet());

            playersInside.addAll(pi);
        }

        connectedTunnels.clear();
        if (tag.contains(TUNNEL_POSITIONS_NBT)) {
            final var positions = GlobalPos.CODEC.listOf()
                    .parse(NbtOps.INSTANCE, tag.get(TUNNEL_POSITIONS_NBT))
                    .getOrThrow(false, CompactMachines.LOGGER::error);

            this.connectedTunnels.addAll(positions);
        }

        if (tag.contains(ROOM_NBT)) {
            int[] room = tag.getIntArray(ROOM_NBT);
            this.roomChunk = new ChunkPos(room[0], room[1]);
        }

        if (tag.contains("owner"))
            owner = tag.getUUID("owner");
    }

    public Optional<ChunkPos> getConnectedRoom() {
        if (roomChunk != null)
            return Optional.of(roomChunk);

        if (level instanceof ServerLevel sl) {
            final var graph = DimensionMachineGraph.forDimension(sl);
            var chunk = graph.getConnectedRoom(worldPosition);
            chunk.ifPresent(c -> this.roomChunk = c);
            return chunk;
        }

        return Optional.empty();
    }

    public Optional<UUID> getOwnerUUID() {
        return Optional.ofNullable(this.owner);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public boolean hasPlayersInside() {
        return !playersInside.isEmpty();
    }

    public boolean hasTunnels() {
        return !connectedTunnels.isEmpty();
    }

    public GlobalPos getLevelPosition() {
        return GlobalPos.of(level.dimension(), worldPosition);
    }

    public void syncConnectedRoom() {
        if (this.level == null || this.level.isClientSide) return;

        if (level instanceof ServerLevel sl) {
            final var graph = DimensionMachineGraph.forDimension(sl);
            graph.getMachineNode(worldPosition).ifPresent(node -> {
                this.graphNode = new WeakReference<>(node);
            });

            this.getConnectedRoom()
                    .flatMap(room -> graph.getRoomNode(this.roomChunk))
                    .ifPresent(roomNode -> this.roomNode = new WeakReference<>(roomNode));

            this.setChanged();
        }
    }

    public void setConnectedRoom(ChunkPos room) {
        if(level instanceof ServerLevel sl) {
            final var dimMachines = DimensionMachineGraph.forDimension(sl);
            dimMachines.connectMachineToRoom(worldPosition, room);
            syncConnectedRoom();
        }
    }

    public void disconnect() {
        if(level instanceof ServerLevel sl) {
            final var dimMachines = DimensionMachineGraph.forDimension(sl);
            dimMachines.disconnect(worldPosition);

            this.roomChunk = null;
            this.graphNode.clear();
            setChanged();
        }
    }
}
