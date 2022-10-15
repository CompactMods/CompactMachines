package dev.compactmods.machines.room.graph;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.room.IRoomLookup;
import dev.compactmods.machines.api.room.IRoomOwnerLookup;
import dev.compactmods.machines.api.room.registration.IMutableRoomRegistration;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.api.room.registration.IRoomSpawnLookup;
import dev.compactmods.machines.codec.NbtListCollector;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.room.MutableRoomRegistration;
import dev.compactmods.machines.room.RoomCodeGenerator;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public class CompactRoomProvider extends SavedData implements IRoomLookup, IRoomOwnerLookup, IRoomSpawnLookup {

    public static final String DATA_NAME = Constants.MOD_ID + "_rooms";
    public static final String NBT_NODE_ID_KEY = "node_id";

    private final Map<String, RoomMetadataNode> metadata;

    private final Map<String, RoomSpawnNode> roomSpawns;

    private final Map<ChunkPos, RoomChunkNode> chunks;
    private final Map<UUID, RoomOwnerNode> owners;

    private final MutableValueGraph<IGraphNode<?>, IGraphEdge> graph;

    private CompactRoomProvider() {
        this.metadata = new HashMap<>();
        this.roomSpawns = new HashMap<>();
        this.chunks = new HashMap<>();
        this.owners = new HashMap<>();
        this.graph = ValueGraphBuilder
                .directed()
                .build();
    }

    public static CompactRoomProvider empty() {
        return new CompactRoomProvider();
    }

    @Nullable
    public static CompactRoomProvider instance() {
        try {
            final ServerLevel level = CompactDimension.forCurrentServer();
            return level.getDataStorage()
                    .computeIfAbsent(CompactRoomProvider::fromDisk, CompactRoomProvider::empty, DATA_NAME);
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
            return null;
        }
    }

    @Nullable
    public static CompactRoomProvider instance(MinecraftServer server) {
        try {
            final ServerLevel level = CompactDimension.forServer(server);
            return level.getDataStorage()
                    .computeIfAbsent(CompactRoomProvider::fromDisk, CompactRoomProvider::empty, DATA_NAME);
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal(e);
            return null;
        }
    }

    public static CompactRoomProvider instance(ServerLevel compactDim) {
            return compactDim.getDataStorage()
                    .computeIfAbsent(CompactRoomProvider::fromDisk, CompactRoomProvider::empty, DATA_NAME);
    }

    private static CompactRoomProvider fromDisk(CompoundTag compoundTag) {
        final var graph = new CompactRoomProvider();

        final HashMap<UUID, RoomMetadataNode> metaNodeIdMap = new HashMap<>();
        if (compoundTag.contains("rooms")) {
            compoundTag.getList("rooms", ListTag.TAG_COMPOUND)
                    .stream()
                    .map(CompoundTag.class::cast)
                    .forEach(roomNode -> {
                        UUID id = roomNode.getUUID(NBT_NODE_ID_KEY);
                        final var node = RoomMetadataNode.CODEC.parse(NbtOps.INSTANCE, roomNode)
                                .getOrThrow(false, CompactMachines.LOGGER::fatal);

                        metaNodeIdMap.put(id, node);
                        graph.metadata.put(node.code(), node);
                    });
        }

        final HashMap<UUID, RoomOwnerNode> roomOwnerNodeMap = new HashMap<>();
        if (compoundTag.contains("owners")) {
            compoundTag.getList("owners", ListTag.TAG_COMPOUND)
                    .stream()
                    .map(CompoundTag.class::cast)
                    .forEach(ownerNode -> {
                        UUID id = ownerNode.getUUID(NBT_NODE_ID_KEY);
                        final var node = RoomOwnerNode.CODEC.parse(NbtOps.INSTANCE, ownerNode)
                                .getOrThrow(false, CompactMachines.LOGGER::fatal);

                        roomOwnerNodeMap.put(id, node);
                        graph.owners.put(node.owner(), node);
                    });
        }

        if (compoundTag.contains("roomOwners")) {
            compoundTag.getList("roomOwners", ListTag.TAG_COMPOUND)
                    .stream()
                    .map(CompoundTag.class::cast)
                    .forEach(roomOwnerConn -> {
                        RoomMetadataNode meta = metaNodeIdMap.get(roomOwnerConn.getUUID("room"));
                        RoomOwnerNode owner = roomOwnerNodeMap.get(roomOwnerConn.getUUID("owner"));
                        graph.graph.putEdgeValue(meta, owner, new RoomOwnerEdge());
                    });
        }

        CompactMachines.LOGGER.debug("Number of rooms loaded from disk: {}", metaNodeIdMap.size());
        return graph;
    }



    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        //region Room Metadata Nodes
        final HashMap<String, UUID> metaNodeIdMap = new HashMap<>();
        metadata.values().forEach(metaNode -> metaNodeIdMap.put(metaNode.code(), UUID.randomUUID()));
        ListTag meta = (ListTag) RoomMetadataNode.CODEC.listOf()
                .encodeStart(NbtOps.INSTANCE, List.copyOf(metadata.values()))
                .getOrThrow(false, CompactMachines.LOGGER::fatal);

        meta.stream()
                .filter(CompoundTag.class::isInstance)
                .map(CompoundTag.class::cast)
                .forEach(mct -> mct.putUUID(NBT_NODE_ID_KEY, metaNodeIdMap.get(mct.getString("code"))));

        tag.put("rooms", meta);
        //endregion

        //region Room Owner nodes
        final HashMap<UUID, UUID> ownerByUuidMap = new HashMap<>();
        owners.values().forEach(ownerNode -> ownerByUuidMap.put(ownerNode.owner(), UUID.randomUUID()));
        ListTag ownerList = (ListTag) RoomOwnerNode.CODEC.listOf()
                .encodeStart(NbtOps.INSTANCE, List.copyOf(owners.values()))
                .getOrThrow(false, CompactMachines.LOGGER::fatal);

        ownerList.stream().map(CompoundTag.class::cast)
                .forEach(oct -> oct.putUUID(NBT_NODE_ID_KEY, ownerByUuidMap.get(oct.getUUID("owner"))));

        tag.put("owners", ownerList);
        //endregion

        //region Room-Owner connections
        if (!metadata.isEmpty() && !owners.isEmpty()) {
            final ListTag roomOwnerConnections = metadata.values()
                    .stream()
                    .map(roomNode -> graph.adjacentNodes(roomNode)
                            .stream()
                            .filter(RoomOwnerNode.class::isInstance)
                            .map(RoomOwnerNode.class::cast)
                            .findFirst()
                            .map(roomOwner -> {
                                UUID roomId = metaNodeIdMap.get(roomNode.code());
                                UUID ownerId = ownerByUuidMap.get(roomOwner.owner());
                                CompoundTag connection = new CompoundTag();
                                connection.putUUID("room", roomId);
                                connection.putUUID("owner", ownerId);
                                return connection;
                            }))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(NbtListCollector.toNbtList());

            tag.put("roomOwners", roomOwnerConnections);
        }
        //endregion

        return tag;
    }

    @Override
    public Stream<IRoomRegistration> allRooms() {
        return metadata.values().stream()
                .map(mn -> mn);
    }

    @Override
    public Stream<IRoomRegistration> findByOwner(UUID owner) {
        if (!owners.containsKey(owner))
            return Stream.empty();

        return graph.adjacentNodes(owners.get(owner)).stream()
                .filter(RoomMetadataNode.class::isInstance)
                .map(RoomMetadataNode.class::cast);
    }

    @Override
    public Optional<IRoomRegistration> findByMachine(IDimensionalBlockPosition machine) {
        return Optional.empty();
    }

    @Override
    public Optional<IRoomRegistration> forRoom(String room) {
        return Optional.ofNullable(metadata.get(room));
    }

    @Override
    public Optional<IRoomRegistration> findByChunk(ChunkPos chunk) {
        if (!isRoomChunk(chunk)) return Optional.empty();
        final var chunkNode = chunks.get(chunk);

        // TODO - Implement with graph
        return Optional.ofNullable(chunkNode.room(this));
    }

    @Override
    public boolean isRoomChunk(ChunkPos chunk) {
        return chunks.containsKey(chunk);
    }

    @Override
    public long count() {
        return metadata.size();
    }

    /**
     * Registers a new room with a specified room code and data from the builder.
     * This assumes that data is coming from a previous source such as a migrator, and
     * will take all values from the returned builder.
     *
     * @param code
     * @param newRoom
     * @return
     */
    public IRoomRegistration registerNew(String code, Function<NewRoomBuilder, NewRoomBuilder> newRoom) {
        final var builder = newRoom.apply(new NewRoomBuilder(code));

        final var roomNode = builder.build();
        this.metadata.put(code, roomNode);
        this.owners.computeIfAbsent(builder.owner, RoomOwnerNode::new);
        final var ownerNode = owners.get(builder.owner);

        graph.putEdgeValue(roomNode, ownerNode, new RoomOwnerEdge());

        // calculate chunks
        roomNode.chunks().forEach(c -> {
            final var roomChunkNode = new RoomChunkNode(c);
            chunks.put(c, roomChunkNode);
            graph.putEdgeValue(roomNode, roomChunkNode, new RoomChunkEdge());
        });
        setDirty();

        return roomNode;
    }

    public IRoomRegistration registerNew(Function<NewRoomBuilder, NewRoomBuilder> newRoom) {
        final var newRoomCode = RoomCodeGenerator.generateRoomId();

        Vec3i location = MathUtil.getRegionPositionByIndex(metadata.size());

        final var builder = newRoom.apply(new NewRoomBuilder(newRoomCode));
        BlockPos newCenter = MathUtil.getCenterWithY(location, ServerConfig.MACHINE_FLOOR_Y.get())
                .above(builder.yOffset());

        builder.setCenter(newCenter);

        final var roomNode = builder.build();
        this.metadata.put(newRoomCode, roomNode);
        this.owners.computeIfAbsent(builder.owner, RoomOwnerNode::new);
        final var ownerNode = owners.get(builder.owner);

        graph.putEdgeValue(roomNode, ownerNode, new RoomOwnerEdge());
        setDirty();

        return roomNode;
    }

    public IMutableRoomRegistration edit(String room) {
        return new MutableRoomRegistration(this, this.metadata.get(room));
    }

    public void resetSpawn(String room) throws NonexistentRoomException {
        if (!metadata.containsKey(room))
            throw new NonexistentRoomException(room);

        final var meta = metadata.get(room);
        final var newSpawn = meta.center().subtract(0, (meta.dimensions().getY() / 2f), 0);
        final var newSpawnNode = new RoomSpawnNode(newSpawn, Vec2.ZERO);

        // TODO - Graph data
        if (!roomSpawns.containsKey(room)) {
            // make new spawn data
            roomSpawns.put(room, newSpawnNode);
        } else {
            // reset spawn data
            roomSpawns.put(room, newSpawnNode);
        }
    }

    @Override
    public Optional<UUID> getRoomOwner(String roomCode) {
        // TODO - Graph data
        if (!metadata.containsKey(roomCode))
            return Optional.empty();

        final var roomNode = metadata.get(roomCode);
        return graph.adjacentNodes(roomNode).stream()
                .filter(RoomOwnerNode.class::isInstance)
                .map(RoomOwnerNode.class::cast)
                .map(RoomOwnerNode::owner)
                .findFirst();
    }
}
