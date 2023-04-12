package dev.compactmods.machines.forge.tunnel.graph.nbt;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.codec.NbtListCollector;
import dev.compactmods.machines.forge.tunnel.Tunnels;
import dev.compactmods.machines.forge.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.tunnel.graph.TunnelMachineEdge;
import dev.compactmods.machines.tunnel.graph.TunnelNode;
import dev.compactmods.machines.tunnel.graph.TunnelTypeEdge;
import dev.compactmods.machines.tunnel.graph.TunnelTypeNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TunnelGraphNbtSerializer {

    private static final Logger LOGS = LogManager.getLogger(Constants.MOD_ID);
    private static final Marker SERIALIZER_MARKER = MarkerManager.getMarker("TunnelGraphSerializer");

    private static final String VERSION = "3.1.0";

    public static CompoundTag serialize(TunnelConnectionGraph graph) {
        final var rootTag = new CompoundTag();

        rootTag.putString("version", VERSION);

        // BEGIN NODES
        CompoundTag nodesGroup = new CompoundTag();

        final var tunnelNodes = serializeTunnels(graph);
        final var tunnelTypeNodes = serializeTunnelTypes(graph);
        final var machineNodes = serializeMachines(graph);

        int numNodes = graph.size();
        var nodeIds = new HashMap<IGraphNode<?>, UUID>(numNodes);

        nodeIds.putAll(tunnelNodes.idMap());
        nodeIds.putAll(tunnelTypeNodes.idMap());
        nodeIds.putAll(machineNodes.idMap());

        nodesGroup.putInt(TunnelGraphNbtKeys.NODE_COUNT, nodeIds.size());
        nodesGroup.put(TunnelGraphNbtKeys.NODE_GROUP_TUNNEL_LIST, tunnelNodes.listTag());
        nodesGroup.put(TunnelGraphNbtKeys.NODE_GROUP_TUNNEL_TYPE_LIST, tunnelTypeNodes.listTag());
        nodesGroup.put(TunnelGraphNbtKeys.NODE_GROUP_MACHINE_LIST, machineNodes.listTag());

        rootTag.put(TunnelGraphNbtKeys.NODE_GROUP, nodesGroup);

        // BEGIN EDGES
        final var edges = new CompoundTag();
        final var machineEdges = serializeEdges(graph, nodeIds, TunnelMachineEdge.CODEC, TunnelMachineEdge.class);
        final var tunnelTypeEdges = serializeEdges(graph, nodeIds, TunnelTypeEdge.CODEC, TunnelTypeEdge.class);

        edges.putInt(TunnelGraphNbtKeys.EDGE_COUNT, machineEdges.size() + tunnelTypeEdges.size());
        edges.put(TunnelGraphNbtKeys.EDGE_GROUP_MACHINE_LIST, machineEdges);
        edges.put(TunnelGraphNbtKeys.EDGE_GROUP_TUNNEL_TYPE_LIST, tunnelTypeEdges);

        rootTag.put(TunnelGraphNbtKeys.EDGE_GROUP, edges);

        return rootTag;
    }

    public static TunnelConnectionGraph fromNbt(CompoundTag tag) {
        final var graph = new TunnelConnectionGraph();
        deserialize(graph, tag);
        return graph;
    }

    public static void deserialize(TunnelConnectionGraph graph, CompoundTag tag) {
        if (!tag.contains(TunnelGraphNbtKeys.NODE_GROUP, Tag.TAG_COMPOUND)) {
            LOGS.info(SERIALIZER_MARKER, "No nodes to load; skipping deserialization.");
            return;
        }

        final var nodesNbt = tag.getCompound(TunnelGraphNbtKeys.NODE_GROUP);

        final var tunnels = deserializeNodes(nodesNbt.getList(TunnelGraphNbtKeys.NODE_GROUP_TUNNEL_LIST, Tag.TAG_COMPOUND), TunnelNode.CODEC);
        LOGS.debug(SERIALIZER_MARKER, "Loaded %s tunnel nodes.".formatted(tunnels.results().size()));

        final var tunnelTypes = deserializeNodes(nodesNbt.getList(TunnelGraphNbtKeys.NODE_GROUP_TUNNEL_TYPE_LIST, Tag.TAG_COMPOUND), TunnelTypeNode.CODEC);
        LOGS.debug(SERIALIZER_MARKER, "Loaded %s tunnel type nodes.".formatted(tunnelTypes.results().size()));

        final var machines = deserializeNodes(nodesNbt.getList(TunnelGraphNbtKeys.NODE_GROUP_MACHINE_LIST, Tag.TAG_COMPOUND), CompactMachineNode.CODEC);
        LOGS.debug(SERIALIZER_MARKER, "Loaded %s machine nodes.".formatted(machines.results().size()));

        final var edgesNbt = tag.getCompound(TunnelGraphNbtKeys.EDGE_GROUP);
        final var tunnelMachineEdges = deserializeEdges(edgesNbt, TunnelGraphNbtKeys.EDGE_GROUP_MACHINE_LIST, TunnelMachineEdge.CODEC);
        final var tunnelTypeEdges = deserializeEdges(edgesNbt, TunnelGraphNbtKeys.EDGE_GROUP_TUNNEL_TYPE_LIST, TunnelTypeEdge.CODEC);

        tunnels.results().forEach((id, node) -> {
            var connectedMachine = tunnelMachineEdges.get(id);
            var connectedType = tunnelTypeEdges.get(id);

            final var machine = machines.results().get(connectedMachine.to());
            final var type = tunnelTypes.results().get(connectedType.to());

            final var definition = Tunnels.getDefinition(type.id());
            graph.registerTunnel(node.position(), definition, machine.dimpos(), connectedMachine.data().side());
        });
    }

    private static <T extends IGraphNode<T>> TunnelGraphNodeDeserializationResult<T> deserializeNodes(
            ListTag list, Codec<T> codec
    ) {
        HashMap<UUID, T> nodeMap = new HashMap<>(list.size());

        list.forEach(nodeTag -> {
            if (!(nodeTag instanceof CompoundTag ct)) return;
            final var node = codec.parse(NbtOps.INSTANCE, ct.getCompound(TunnelGraphNbtKeys.NODE_DATA))
                    .getOrThrow(false, e -> LOGS.error(SERIALIZER_MARKER, e));

            final var nodeId = ct.getUUID(TunnelGraphNbtKeys.NODE_ID);
            nodeMap.put(nodeId, node);
        });

        return new TunnelGraphNodeDeserializationResult<>(nodeMap);
    }

    private static <T extends IGraphEdge<T>> HashMap<UUID, TunnelGraphEdgeDeserializationResult<T>> deserializeEdges(
            CompoundTag edgesNbt, String listKey, Codec<T> codec
    ) {
        final var connections = new HashMap<UUID, TunnelGraphEdgeDeserializationResult<T>>();
        edgesNbt.getList(listKey, Tag.TAG_COMPOUND).forEach(edgeTag -> {
            if (!(edgeTag instanceof CompoundTag ct)) return;

            final var fromId = ct.getUUID(TunnelGraphNbtKeys.EDGE_CONNECTION_FROM_ID);
            final var toId = ct.getUUID(TunnelGraphNbtKeys.EDGE_CONNECTION_TO_ID);
            final var edgeData = codec.parse(NbtOps.INSTANCE, ct.getCompound(TunnelGraphNbtKeys.EDGE_CONNECTION_DATA))
                    .getOrThrow(false, e -> LOGS.error(SERIALIZER_MARKER, e));

            var result = new TunnelGraphEdgeDeserializationResult<>(fromId, toId, edgeData);
            connections.putIfAbsent(fromId, result);
        });

        return connections;
    }

    private static <T extends IGraphNode<T>> TunnelGraphNodeSerializationResult<T> serializeNodes(
            final TunnelConnectionGraph connections, final Codec<T> serializer, final Class<T> nodeType
    ) {
        HashMap<T, UUID> nodeIds = new HashMap<>();
        final ListTag listNbt = connections.nodes(nodeType)
                .map(node -> {
                    var id = UUID.randomUUID();
                    nodeIds.put(node, id);
                    var nodeNbt = serializer.encodeStart(NbtOps.INSTANCE, node)
                            .getOrThrow(false, err -> LOGS.error(SERIALIZER_MARKER, err));

                    var tag = new CompoundTag();
                    tag.putUUID(TunnelGraphNbtKeys.NODE_ID, id);
                    tag.put(TunnelGraphNbtKeys.NODE_DATA, nodeNbt);
                    return tag;
                })
                .collect(NbtListCollector.toNbtList());

        return new TunnelGraphNodeSerializationResult<T>(nodeIds, listNbt);
    }

    private static <E extends IGraphEdge<E>> ListTag serializeEdges(
            final TunnelConnectionGraph connections, final Map<IGraphNode<?>, UUID> nodeIds,
            final Codec<E> serializer, Class<E> edgeType
    ) {
        return connections.edges(edgeType)
                .map(tme -> {
                    var edge = tme.edgeValue();
                    var edgeData = serializer.encodeStart(NbtOps.INSTANCE, edge)
                            .getOrThrow(false, err -> LOGS.error(SERIALIZER_MARKER, err));

                    CompoundTag edgeInfo = new CompoundTag();
                    edgeInfo.putUUID(TunnelGraphNbtKeys.EDGE_CONNECTION_FROM_ID, nodeIds.get(tme.endpoints().nodeU()));
                    edgeInfo.putUUID(TunnelGraphNbtKeys.EDGE_CONNECTION_TO_ID, nodeIds.get(tme.endpoints().nodeV()));
                    edgeInfo.put(TunnelGraphNbtKeys.EDGE_CONNECTION_DATA, edgeData);

                    return edgeInfo;
                }).collect(NbtListCollector.toNbtList());
    }

    public static TunnelGraphNodeSerializationResult<TunnelNode> serializeTunnels(final TunnelConnectionGraph connections) {
        LOGS.debug(SERIALIZER_MARKER, "Starting tunnel node serialization.");
        final var result = serializeNodes(connections, TunnelNode.CODEC, TunnelNode.class);
        LOGS.debug(SERIALIZER_MARKER, "Done serializing node information: {} tunnel nodes serialized.", result.listTag().size());
        return result;
    }

    public static TunnelGraphNodeSerializationResult<TunnelTypeNode> serializeTunnelTypes(final TunnelConnectionGraph connections) {
        LOGS.debug(SERIALIZER_MARKER, "Starting tunnel type node serialization.");
        final var result = serializeNodes(connections, TunnelTypeNode.CODEC, TunnelTypeNode.class);
        LOGS.debug(SERIALIZER_MARKER, "Done serializing node information: {} tunnel type nodes serialized.", result.listTag().size());
        return result;
    }

    public static TunnelGraphNodeSerializationResult<CompactMachineNode> serializeMachines(final TunnelConnectionGraph connections) {
        LOGS.debug(SERIALIZER_MARKER, "Starting machine node serialization.");
        final var result = serializeNodes(connections, CompactMachineNode.CODEC, CompactMachineNode.class);
        LOGS.debug(SERIALIZER_MARKER, "Done serializing node information: {} machine nodes serialized.", result.listTag().size());
        return result;
    }
}
