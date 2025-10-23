package dev.compactmods.machines.room.spatial;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.spatial.IRoomBoundaries;
import dev.compactmods.machines.api.room.spatial.IRoomChunkManager;
import dev.compactmods.machines.api.room.spatial.IRoomChunks;
import dev.compactmods.feather.MemoryGraph;
import dev.compactmods.feather.edge.GraphEdge;
import dev.compactmods.machines.room.graph.GraphNodes;
import dev.compactmods.machines.room.graph.edge.RoomChunkEdge;
import dev.compactmods.machines.room.graph.node.RoomChunkNode;
import dev.compactmods.machines.room.graph.node.RoomReferenceNode;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.world.level.ChunkPos;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class GraphChunkManager implements IRoomChunkManager {

    private final MemoryGraph graph;
    private final Map<ChunkPos, RoomChunkNode> chunks;

    public GraphChunkManager() {
        this.graph = new MemoryGraph();
        this.chunks = new HashMap<>();

        CompactMachines.roomRegistrar()
                .allRooms()
                .forEach(inst -> calculateChunks(inst.code(), inst.boundaries()));
    }

    @Override
    public void calculateChunks(String roomCode, IRoomBoundaries boundaries) {
        final var outer = boundaries.outerBounds();
        final var allInside = MathUtil.getChunksFromAABB(outer).collect(Collectors.toSet());

        final var ref = new RoomReferenceNode(roomCode);
        graph.addNode(ref);

        for (var c : allInside) {
            final var chunk = new RoomChunkNode(UUID.randomUUID(), new RoomChunkNode.Data(c));
            graph.addNode(chunk);
            graph.connectNodes(ref, chunk, new RoomChunkEdge(ref, chunk));
            chunks.put(c, chunk);
        }
    }

    @Override
    public Optional<String> findRoomByChunk(ChunkPos chunk) {
        if (!chunks.containsKey(chunk)) return Optional.empty();
        final var chunkNode = chunks.get(chunk);

        return graph.inboundEdges(chunkNode, RoomReferenceNode.class)
                .map(GraphEdge::source)
                .map(WeakReference::get)
                .filter(Objects::nonNull)
                .map(RoomReferenceNode::code)
                .findFirst();
    }

    @Override
    public IRoomChunks get(String room) {
        final var regNode = graph.nodes(RoomReferenceNode.class)
                .filter(rn -> rn.code().equals(room))
                .findFirst();

        return regNode.map(node -> {
            final var chunks = graph.outboundEdges(GraphNodes.ROOM_CHUNKS, node)
                    .map(GraphEdge::target)
                    .map(WeakReference::get)
                    .filter(Objects::nonNull)
                    .peek(chunkNode -> this.chunks.putIfAbsent(chunkNode.data().chunk(), chunkNode))
                    .map(c -> c.data().chunk())
                    .collect(Collectors.toSet());

            return new RoomChunks(chunks);
        }).orElseThrow();
    }
}
