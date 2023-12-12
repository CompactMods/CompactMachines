package dev.compactmods.machines.forge.room.data;

import com.google.common.graph.MutableValueGraph;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.codec.NbtListCollector;
import dev.compactmods.machines.graph.GraphTraversalHelper;
import dev.compactmods.machines.graph.edge.IGraphEdge;
import dev.compactmods.machines.graph.node.IGraphNode;
import dev.compactmods.machines.room.RoomCodeGenerator;
import dev.compactmods.machines.room.graph.NewRoomBuilder;
import dev.compactmods.machines.room.graph.edge.RoomChunkEdge;
import dev.compactmods.machines.room.graph.edge.RoomOwnerEdge;
import dev.compactmods.machines.room.graph.node.RoomChunkNode;
import dev.compactmods.machines.room.graph.node.RoomOwnerNode;
import dev.compactmods.machines.room.graph.node.RoomRegistrationNode;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public class AllRoomsDataFile extends SavedData {

    public static final Logger LOGS = LogManager.getLogger();

    public static final String DATA_NAME = Constants.MOD_ID + "_rooms";
    public static final String NBT_NODE_ID_KEY = "node_id";

    private final Map<String, RoomRegistrationNode> registrationNodes;

    private final Map<UUID, RoomOwnerNode> owners;


    private AllRoomsDataFile() {
        this.registrationNodes = new HashMap<>();
        this.owners = new HashMap<>();
    }

    public static AllRoomsDataFile empty() {
        return new AllRoomsDataFile();
    }

    @Nullable
    public static AllRoomsDataFile instance(MinecraftServer server) {
        try {
            final ServerLevel level = CompactDimension.forServer(server);
            return level.getDataStorage()
                    .computeIfAbsent(AllRoomsDataFile::fromDisk, AllRoomsDataFile::empty, DATA_NAME);
        } catch (MissingDimensionException e) {
            LOGS.fatal(e);
            return null;
        }
    }

    public static AllRoomsDataFile instance(ServerLevel compactDim) {
        return compactDim.getDataStorage()
                .computeIfAbsent(AllRoomsDataFile::fromDisk, AllRoomsDataFile::empty, DATA_NAME);
    }

    public static AllRoomsDataFile fromDisk(CompoundTag compoundTag) {
        final var graph = new AllRoomsDataFile();

        final HashMap<UUID, RoomRegistrationNode> metaNodeIdMap = new HashMap<>();
        if (compoundTag.contains("rooms")) {
            compoundTag.getList("rooms", ListTag.TAG_COMPOUND)
                    .stream()
                    .map(CompoundTag.class::cast)
                    .forEach(roomNode -> {
                        UUID id = roomNode.getUUID(NBT_NODE_ID_KEY);
                        final var node = RoomRegistrationNode.CODEC.parse(NbtOps.INSTANCE, roomNode)
                                .getOrThrow(false, LOGS::fatal);

                        metaNodeIdMap.put(id, node);
                        graph.registrationNodes.put(node.code(), node);

                        node.chunks().forEach(chunk -> {
                            RoomChunkNode chunkNode = new RoomChunkNode(chunk);
                            graph.graph.putEdgeValue(node, chunkNode, new RoomChunkEdge());
                            graph.chunks.put(chunk, chunkNode);
                        });
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
                                .getOrThrow(false, LOGS::fatal);

                        roomOwnerNodeMap.put(id, node);
                        graph.owners.put(node.owner(), node);
                    });
        }

        if (compoundTag.contains("roomOwners")) {
            compoundTag.getList("roomOwners", ListTag.TAG_COMPOUND)
                    .stream()
                    .map(CompoundTag.class::cast)
                    .forEach(roomOwnerConn -> {
                        RoomRegistrationNode meta = metaNodeIdMap.get(roomOwnerConn.getUUID("room"));
                        RoomOwnerNode owner = roomOwnerNodeMap.get(roomOwnerConn.getUUID("owner"));
                        graph.graph.putEdgeValue(meta, owner, new RoomOwnerEdge());
                    });
        }

        LOGS.debug("Number of rooms loaded from disk: {}", metaNodeIdMap.size());
        return graph;
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag tag) {
        //region Room Metadata Nodes
        final HashMap<String, UUID> metaNodeIdMap = new HashMap<>();
        registrationNodes.values().forEach(metaNode -> metaNodeIdMap.put(metaNode.code(), UUID.randomUUID()));
        ListTag meta = (ListTag) RoomRegistrationNode.CODEC.listOf()
                .encodeStart(NbtOps.INSTANCE, List.copyOf(registrationNodes.values()))
                .getOrThrow(false, LOGS::fatal);

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
                .getOrThrow(false, LOGS::fatal);

        ownerList.stream().map(CompoundTag.class::cast)
                .forEach(oct -> oct.putUUID(NBT_NODE_ID_KEY, ownerByUuidMap.get(oct.getUUID("owner"))));

        tag.put("owners", ownerList);
        //endregion

        //region Room-Owner connections
        if (!registrationNodes.isEmpty() && !owners.isEmpty()) {
            final ListTag roomOwnerConnections = registrationNodes.values()
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
        return registrationNodes.values().stream()
                .map(mn -> mn);
    }

    @Override
    public boolean isRegistered(String room) {
        return false;
    }

    @Override
    public Stream<IRoomRegistration> findByOwner(UUID owner) {
        if (!owners.containsKey(owner))
            return Stream.empty();

        return graph.adjacentNodes(owners.get(owner)).stream()
                .filter(RoomRegistrationNode.class::isInstance)
                .map(RoomRegistrationNode.class::cast);
    }

    @Override
    public Optional<IRoomRegistration> get(String room) {
        return Optional.ofNullable(registrationNodes.get(room));
    }

    @Override
    public Optional<IRoomRegistration> findByChunk(ChunkPos chunk) {

    }

    @Override
    public boolean isRoomChunk(ChunkPos chunk) {
        return chunks.containsKey(chunk);
    }

    @Override
    public long count() {
        return registrationNodes.size();
    }

    private IRoomRegistration finalizeNew(String code, RoomRegistrationNode roomNode, UUID owner) {
        this.registrationNodes.put(code, roomNode);
        this.owners.computeIfAbsent(owner, RoomOwnerNode::new);
        final var ownerNode = owners.get(owner);

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
        return finalizeNew(code, roomNode, builder.owner);
    }

    public IRoomRegistration registerNew(Function<NewRoomBuilder, NewRoomBuilder> newRoom) {
        final var newRoomCode = RoomCodeGenerator.generateRoomId();

        Vec3i location = MathUtil.getRegionPositionByIndex(registrationNodes.size());

        final var builder = newRoom.apply(new NewRoomBuilder(newRoomCode));
        BlockPos newCenter = MathUtil.getCenterWithY(location, 40).above(builder.yOffset());

        builder.setCenter(newCenter);

        final var roomNode = builder.build();
        return finalizeNew(newRoomCode, roomNode, builder.owner);
    }

    /**
     * @deprecated Fetch an instance of {@link dev.compactmods.machines.api.room.spawn.IRoomSpawnManager} and use the method there.
     * @param room
     * @throws NonexistentRoomException
     */
    @Deprecated(forRemoval = true)
    public void resetSpawn(String room) throws NonexistentRoomException {

    }

    @Override
    public UUID getRoomOwner(String roomCode) throws NonexistentRoomException {
        if (!registrationNodes.containsKey(roomCode))
            throw new NonexistentRoomException(roomCode);

        final var roomNode = registrationNodes.get(roomCode);
        return GraphTraversalHelper.predecessors(graph, roomNode, RoomOwnerNode.class)
                .map(RoomOwnerNode::owner)
                .findFirst()
                .orElseThrow(() -> new NonexistentRoomException(roomCode));
    }

    @ApiStatus.Internal
    public MutableValueGraph<IGraphNode<?>, IGraphEdge<?>> getGraph() {
        return graph;
    }

    public Optional<RoomRegistrationNode> roomNode(String roomCode) {
        return Optional.ofNullable(registrationNodes.get(roomCode));
    }
}
