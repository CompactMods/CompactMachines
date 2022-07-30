package dev.compactmods.machines.tunnel.graph;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.codec.NbtListCollector;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityTunnel;
import dev.compactmods.machines.tunnel.Tunnels;
import dev.compactmods.machines.graph.*;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.machine.graph.MachineRoomEdge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a room's tunnel connections in a graph-style format.
 * This should be accessed through the saved data for specific machine room chunks.
 */
public class TunnelConnectionGraph extends SavedData implements INBTSerializable<CompoundTag> {

    /**
     * The full data graph. Contains tunnel nodes, machine ids, and tunnel type information.
     */
    private final MutableValueGraph<IGraphNode, IGraphEdge> graph;

    /**
     * Quick access to tunnel information for specific locations.
     */
    private final Map<BlockPos, TunnelNode> tunnels;

    /**
     * Quick access to machine information nodes.
     */
    private final Map<IDimensionalBlockPosition, CompactMachineNode> machines;

    /**
     * Quick access to tunnel definition nodes.
     */
    private final Map<ResourceLocation, TunnelTypeNode> tunnelTypes;

    private TunnelConnectionGraph() {
        graph = ValueGraphBuilder
                .directed()
                .build();

        tunnels = new HashMap<>();
        machines = new HashMap<>();
        tunnelTypes = new HashMap<>();
    }

    private TunnelConnectionGraph(CompoundTag nbt) {
        this();
        this.deserializeNBT(nbt);
    }

    public static TunnelConnectionGraph forRoom(ServerLevel compactDim, ChunkPos room) {
        final var key = getDataFilename(room);
        return compactDim.getDataStorage().computeIfAbsent(
                TunnelConnectionGraph::new,
                TunnelConnectionGraph::new,
                key
        );
    }

    @Nonnull
    @Override
    public CompoundTag save(CompoundTag tag) {
        var gData = this.serializeNBT();
        tag.put("graph", gData);
        return tag;
    }

    public static String getDataFilename(ChunkPos room) {
        return "tunnels_" + room.x + "_" + room.z;
    }

    /**
     * Finds which machine a tunnel is connected to.
     *
     * @param tunnel The tunnel to find a connection for.
     * @return The id of the connected machine.
     */
    public Optional<IDimensionalBlockPosition> connectedMachine(BlockPos tunnel) {
        if (!tunnels.containsKey(tunnel))
            return Optional.empty();

        var tNode = tunnels.get(tunnel);
        return graph.successors(tNode)
                .stream()
                .filter(CompactMachineNode.class::isInstance)
                .map(CompactMachineNode.class::cast)
                .findFirst()
                .map(CompactMachineNode::dimpos);
    }

    /**
     * Registers a tunnel as being connected to a machine on a particular side.
     * If the tunnel already is registered, this will report a failure.
     *
     * @param tunnelPos The position of the tunnel inside the room.
     * @param type      The type of tunnel being registered.
     * @param machine   The machine the tunnel is to be connected to.
     * @param side      The side of the machine the tunnel is connecting to.
     * @return True if the connection could be established; false if the tunnel type is already registered for the given side.
     */
    public boolean registerTunnel(BlockPos tunnelPos, TunnelDefinition type, IDimensionalBlockPosition machine, Direction side) {
        // First we need to get the machine the tunnel is trying to connect to
        var machineNode = getOrCreateMachineNode(machine);
        var tunnelNode = getOrCreateTunnelNode(tunnelPos);
        if (graph.hasEdgeConnecting(tunnelNode, machineNode)) {
            // connection already formed between the tunnel at pos and the machine
            CompactMachines.LOGGER.info("Tunnel already registered for machine {} at position {}.",
                    machine,
                    tunnelPos);

            return false;
        }

        var tunnelTypeNode = getOrCreateTunnelTypeNode(type);

        // no tunnels registered for side yet - free to make new tunnel node
        var newTM = graph.putEdgeValue(tunnelNode, machineNode, new TunnelMachineEdge(side));
        var newTT = graph.putEdgeValue(tunnelNode, tunnelTypeNode, new TunnelTypeEdge());

        setDirty();
        return true;
    }

    private void createTunnelAndLink(TunnelNode newTunnel, Direction side, CompactMachineNode machNode, TunnelTypeNode typeNode) {

    }

    @Nonnull
    public TunnelNode getOrCreateTunnelNode(BlockPos tunnelPos) {
        if (tunnels.containsKey(tunnelPos))
            return tunnels.get(tunnelPos);

        var newTunnel = new TunnelNode(tunnelPos);
        tunnels.put(tunnelPos, newTunnel);
        graph.addNode(newTunnel);
        setDirty();

        return newTunnel;
    }

    public CompactMachineNode getOrCreateMachineNode(IDimensionalBlockPosition machine) {
        var machineRegistered = machines.containsKey(machine);
        CompactMachineNode node;
        if (!machineRegistered) {
            node = new CompactMachineNode(machine.dimensionKey(), machine.getBlockPosition());
            machines.put(machine, node);
            graph.addNode(node);
            setDirty();
        } else {
            node = machines.get(machine);
        }

        return node;
    }

    public TunnelTypeNode getOrCreateTunnelTypeNode(TunnelDefinition definition) {
        final ResourceLocation id = Tunnels.getRegistryId(definition);

        if (tunnelTypes.containsKey(id))
            return tunnelTypes.get(id);

        TunnelTypeNode newType = new TunnelTypeNode(id);
        tunnelTypes.put(id, newType);
        graph.addNode(newType);
        setDirty();
        return newType;
    }

    /**
     * Gets the number of registered nodes in the graph.
     */
    public int size() {
        return graph.nodes().size();
    }

    @Deprecated(forRemoval = true, since = "5.0.0")
    public Stream<TunnelNode> getTunnelNodesByType(TunnelDefinition type) {
        final var id = Tunnels.getRegistryId(type);
        return getTunnelNodesByType(id);
    }

    public Stream<TunnelNode> getTunnelNodesByType(ResourceLocation type) {
        var defNode = tunnelTypes.get(type);
        if (defNode == null)
            return Stream.empty();

        return graph.adjacentNodes(defNode)
                .stream()
                .filter(TunnelNode.class::isInstance)
                .map(TunnelNode.class::cast);
    }

    @Deprecated(forRemoval = true, since = "5.0.0")
    public Set<BlockPos> getTunnelsByType(TunnelDefinition type) {
        return getTunnelNodesByType(type)
                .map(TunnelNode::position)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());
    }

    public Optional<Direction> getTunnelSide(TunnelNode node) {
        return graph.adjacentNodes(node).stream()
                .filter(CompactMachineNode.class::isInstance)
                .map(mn -> graph.edgeValue(node, mn))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TunnelMachineEdge.class::cast)
                .map(TunnelMachineEdge::side)
                .findFirst();
    }

    public Optional<Direction> getTunnelSide(BlockPos pos) {
        if (!tunnels.containsKey(pos))
            return Optional.empty();

        var node = tunnels.get(pos);
        return graph.adjacentNodes(node).stream()
                .filter(CompactMachineNode.class::isInstance)
                .map(mn -> graph.edgeValue(node, mn))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TunnelMachineEdge.class::cast)
                .map(TunnelMachineEdge::side)
                .findFirst();
    }

    public Optional<TunnelMachineInfo> getTunnelInfo(BlockPos tunnel) {
        if (!tunnels.containsKey(tunnel))
            return Optional.empty();

        var node = tunnels.get(tunnel);
        var typeNode = graph.successors(node).stream()
                .filter(TunnelTypeNode.class::isInstance)
                .map(TunnelTypeNode.class::cast)
                .findFirst()
                .orElseThrow();

        var mach = connectedMachine(tunnel).orElseThrow();
        var side = getTunnelSide(tunnel).orElseThrow();
        var type = typeNode.id();

        return Optional.of(new TunnelMachineInfo(tunnel, type, new LevelBlockPosition(mach), side));
    }

    public Stream<TunnelMachineInfo> tunnels() {
        return tunnels.keySet().stream()
                .map(this::getTunnelInfo)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public Stream<IGraphNode> nodes() {
        return graph.nodes().stream();
    }

    @Override
    public CompoundTag serializeNBT() {
        cleanupOrphans();

        CompoundTag tag = new CompoundTag();

        HashMap<IGraphNode, UUID> nodeIds = new HashMap<>();

        final var nodeReg = Graph.NODE_TYPE_REG.get();
        final var nodeRegCodec = nodeReg.getCodec()
                .dispatchStable(IGraphNode::getType, IGraphNodeType::codec);

        var nodeList = nodes().map(node -> {
            CompoundTag nodeInfo = new CompoundTag();

            var encoded = nodeRegCodec.encodeStart(NbtOps.INSTANCE, node);
            var nodeEncoded = encoded.getOrThrow(false, CompactMachines.LOGGER::error);

            var id = UUID.randomUUID();
            nodeInfo.putUUID("id", id);
            nodeInfo.put("data", nodeEncoded);
            nodeIds.put(node, id);
            return nodeInfo;
        }).collect(NbtListCollector.toNbtList());

        tag.put("nodes", nodeList);

        ListTag edges = new ListTag();
        for (var edge : graph.edges()) {
            CompoundTag edgeInfo = new CompoundTag();

            //noinspection OptionalGetWithoutIsPresent
            var realEdge = graph.edgeValue(edge).get();
            var codec = realEdge.getEdgeType().codec();

            var encoded = codec.encodeStart(NbtOps.INSTANCE, realEdge);
            var edgeEnc = encoded.getOrThrow(false, CompactMachines.LOGGER::error);

            edgeInfo.putUUID("from", nodeIds.get(edge.nodeU()));
            edgeInfo.putUUID("to", nodeIds.get(edge.nodeV()));
            edgeInfo.put("data", edgeEnc);

            edges.add(edgeInfo);
        }

        tag.put("edges", edges);

        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (!tag.contains("graph")) return;

        final var graphRoot = tag.getCompound("graph");

        final var nodeReg = Graph.NODE_TYPE_REG.get();
        final var nodeRegCodec = nodeReg.getCodec()
                .dispatchStable(IGraphNode::getType, IGraphNodeType::codec);

        final var edgeRegCodec = Graph.EDGE_TYPE_REG.get().getCodec()
                .dispatchStable(IGraphEdge::getEdgeType, IGraphEdgeType::codec);

        final var nodes = graphRoot.getList("nodes", Tag.TAG_COMPOUND);
        HashMap<UUID, IGraphNode> nodeMap = new HashMap<>(nodes.size());

        if (graphRoot.contains("nodes", Tag.TAG_LIST)) {
            for (var nodeNbt : nodes) {
                if (!(nodeNbt instanceof CompoundTag nt))
                    continue;

                if (!nt.contains("data") || !nt.hasUUID("id"))
                    continue;

                UUID nodeId = nt.getUUID("id");
                CompoundTag nodeData = nt.getCompound("data");

                var result = nodeRegCodec.parse(NbtOps.INSTANCE, nodeData)
                        .getOrThrow(false, CompactMachines.LOGGER::error);

                if (result == null) continue;

                try {
                    if (result instanceof CompactMachineNode m) {
                        graph.addNode(m);
                        machines.put(m.dimpos(), m);
                        nodeMap.putIfAbsent(nodeId, m);
                    }

                    if (result instanceof TunnelNode t) {
                        graph.addNode(t);
                        tunnels.put(t.position(), t);
                        nodeMap.putIfAbsent(nodeId, t);
                    }

                    if (result instanceof TunnelTypeNode tt) {
                        graph.addNode(tt);
                        tunnelTypes.put(tt.id(), tt);
                        nodeMap.putIfAbsent(nodeId, tt);
                    }

                } catch (RuntimeException ignored) {
                }
            }
        }

        // No edges - skip rest of processing
        if (graphRoot.contains("edges", Tag.TAG_LIST)) {
            final var edgeTags = graphRoot.getList("edges", Tag.TAG_COMPOUND);
            for (var edgeTag : edgeTags) {
                if (!(edgeTag instanceof CompoundTag edge))
                    continue;

                // invalid edge data
                if (!edge.contains("data") || !edge.hasUUID("from") || !edge.hasUUID("to"))
                    continue;

                var nodeFrom = nodeMap.get(edge.getUUID("from"));
                var nodeTo = nodeMap.get(edge.getUUID("to"));

                if (nodeFrom == null || nodeTo == null)
                    continue;

                var edgeData = edgeRegCodec.parse(NbtOps.INSTANCE, edge.getCompound("data"))
                        .getOrThrow(false, CompactMachines.LOGGER::error);

                graph.putEdgeValue(nodeFrom, nodeTo, edgeData);
            }
        }
    }

    public boolean hasTunnel(BlockPos location) {
        return tunnels.containsKey(location);
    }

    public <T> Stream<BlockPos> getTunnelsSupporting(LevelBlockPosition machine, Direction side, Capability<T> capability) {
        final var node = machines.get(machine);
        if (node == null) return Stream.empty();

        return getTunnelsForSide(machine, side)
                .filter(sided -> graph.successors(sided).stream()
                        .filter(TunnelTypeNode.class::isInstance)
                        .map(TunnelTypeNode.class::cast)
                        .anyMatch(ttn -> {
                            var def = Tunnels.getDefinition(ttn.id());
                            if (!(def instanceof CapabilityTunnel<?> tcp))
                                return false;

                            return tcp.getSupportedCapabilities().contains(capability);
                        })).map(TunnelNode::position);
    }

    public Stream<TunnelDefinition> getTypesForSide(LevelBlockPosition machine, Direction side) {
        final var node = machines.get(machine);
        if (node == null) return Stream.empty();

        return getTunnelsForSide(machine, side)
                .flatMap(tn -> graph.successors(tn).stream())
                .filter(TunnelTypeNode.class::isInstance)
                .map(TunnelTypeNode.class::cast)
                .map(type -> Tunnels.getDefinition(type.id()))
                .distinct();
    }

    public Stream<TunnelNode> getTunnelsForSide(IDimensionalBlockPosition machine, Direction side) {
        final var node = machines.get(machine);
        if (node == null) return Stream.empty();

        return graph.incidentEdges(node).stream()
                .filter(e -> graph.edgeValue(e)
                        .map(ed -> ed instanceof TunnelMachineEdge tme && tme.side() == side)
                        .orElse(false)
                )
                .map(EndpointPair::nodeU)
                .filter(TunnelNode.class::isInstance)
                .map(TunnelNode.class::cast);
    }

    @Deprecated(forRemoval = true, since = "5.0.0")
    public Stream<Direction> getTunnelSides(TunnelDefinition type) {
        final var id = Tunnels.getRegistryId(type);
        return getTunnelSides(id);
    }

    public Stream<Direction> getTunnelSides(ResourceLocation type) {
        if (!tunnelTypes.containsKey(type))
            return Stream.empty();

        return getTunnelNodesByType(type)
                .map(this::getTunnelSide)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public void clear() {
        for (var machine : machines.values())
            graph.removeNode(machine);
        machines.clear();

        for (var t : tunnelTypes.values())
            graph.removeNode(t);
        tunnelTypes.clear();

        for (var tun : tunnels.values())
            graph.removeNode(tun);
        tunnels.clear();
    }

    @Deprecated(forRemoval = true, since = "5.0.0")
    public Stream<BlockPos> getMachineTunnels(IDimensionalBlockPosition machine, TunnelDefinition type) {
        return getTunnelNodesByType(type)
                .map(TunnelNode::position)
                .filter(position -> connectedMachine(position).map(machine::equals).orElse(false))
                .map(BlockPos::immutable);
    }

    /**
     * Unlinks a tunnel at a specified point inside the machine room.
     *
     * @param pos Tunnel position inside the room.
     */
    public void unregister(BlockPos pos) {
        if (!hasTunnel(pos))
            return;

        CompactMachines.LOGGER.debug("Unregistering tunnel at {}", pos);

        final var existing = tunnels.get(pos);
        graph.removeNode(existing);
        tunnels.remove(pos);

        cleanupOrphanedTypes();
        cleanupOrphanedMachines();

        setDirty();
    }

    private void cleanupOrphans() {
        cleanupOrphanedTunnels();
        cleanupOrphanedTypes();
        cleanupOrphanedMachines();
    }

    private void cleanupOrphanedTypes() {
        HashSet<ResourceLocation> removedTypes = new HashSet<>();
        tunnelTypes.forEach((type, node) -> {
            if (graph.degree(node) == 0) {
                graph.removeNode(node);
                removedTypes.add(type);
            }
        });

        if (!removedTypes.isEmpty()) {
            CompactMachines.LOGGER.debug("Removed {} tunnel type nodes during cleanup.", removedTypes.size());
            removedTypes.forEach(tunnelTypes::remove);
            setDirty();
        }

    }

    private void cleanupOrphanedTunnels() {
        HashSet<BlockPos> removed = new HashSet<>();
        tunnels.forEach((pos, node) -> {
            if (graph.degree(node) == 0) {
                graph.removeNode(node);
                removed.add(pos);
            }
        });

        if (!removed.isEmpty()) {
            CompactMachines.LOGGER.debug("Removed {} tunnel nodes during cleanup.", removed.size());
            removed.forEach(tunnels::remove);
            setDirty();
        }
    }

    private void cleanupOrphanedMachines() {
        HashSet<IDimensionalBlockPosition> removed = new HashSet<>();
        machines.forEach((machine, node) -> {
            if (graph.degree(node) == 0) {
                graph.removeNode(node);
                removed.add(machine);
            }
        });

        if (!removed.isEmpty()) {
            CompactMachines.LOGGER.debug("Removed {} machine nodes during cleanup.", removed.size());
            removed.forEach(machines::remove);
            setDirty();
        }
    }

    public void rotateTunnel(BlockPos tunnel, Direction newSide) {
        if (!tunnels.containsKey(tunnel))
            return;

        final var connected = connectedMachine(tunnel);
        connected.ifPresent(machine -> {
            if (!machines.containsKey(machine))
                return;

            final var t = tunnels.get(tunnel);
            final var m = machines.get(machine);
            graph.removeEdge(t, m);
            graph.putEdgeValue(t, m, new TunnelMachineEdge(newSide));

            setDirty();
        });
    }

    public Stream<IDimensionalBlockPosition> getMachines() {
        return this.machines.keySet().stream();
    }

    public Stream<BlockPos> getConnections(IDimensionalBlockPosition machine) {
        if (!machines.containsKey(machine))
            return Stream.empty();

        final var mNode = machines.get(machine);
        return graph.incidentEdges(mNode).stream()
                .filter(e -> graph.edgeValue(e).orElseThrow() instanceof MachineRoomEdge)
                .map(edge -> {
                    if (edge.nodeU() instanceof TunnelNode cmn) return cmn;
                    if (edge.nodeV() instanceof TunnelNode cmn2) return cmn2;
                    return null;
                }).filter(Objects::nonNull).map(TunnelNode::position);

    }

    public boolean hasAnyConnectedTo(IDimensionalBlockPosition machine) {
        return getConnections(machine).findAny().isPresent();
    }

    public void rebind(BlockPos tunnel, IDimensionalBlockPosition newMachine, Direction side) {
        CompactMachines.LOGGER.debug("Rebinding tunnel at {} to machine {}", tunnel, newMachine);

        final var tunnelNode = getOrCreateTunnelNode(tunnel);
        final var newMachineNode = getOrCreateMachineNode(newMachine);
        graph.putEdgeValue(tunnelNode, newMachineNode, new TunnelMachineEdge(side));
        setDirty();
    }
}
