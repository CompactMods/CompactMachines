package dev.compactmods.machines.tunnel.graph;

import com.google.common.graph.*;
import com.mojang.serialization.Codec;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityTunnel;
import dev.compactmods.machines.api.codec.NbtListCollector;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.graph.CompactGraphs;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.machine.graph.MachineLinkEdge;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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
public class TunnelConnectionGraph implements INBTSerializable<CompoundTag> {

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
    private final Map<Integer, CompactMachineNode> machines;

    /**
     * Quick access to tunnel definition nodes.
     */
    private final Map<ResourceLocation, TunnelTypeNode> tunnelTypes;

    public TunnelConnectionGraph() {
        graph = ValueGraphBuilder
                .directed()
                .build();

        tunnels = new HashMap<>();
        machines = new HashMap<>();
        tunnelTypes = new HashMap<>();
    }

    /**
     * Finds which machine a tunnel is connected to.
     *
     * @param tunnel The tunnel to find a connection for.
     * @return The id of the connected machine.
     */
    public Optional<Integer> connectedMachine(BlockPos tunnel) {
        if (!tunnels.containsKey(tunnel))
            return Optional.empty();

        var tNode = tunnels.get(tunnel);
        return graph.successors(tNode)
                .stream()
                .filter(CompactMachineNode.class::isInstance)
                .findFirst()
                .map(mNode -> ((CompactMachineNode) mNode).machineId());
    }

    /**
     * Registers a tunnel as being connected to a machine on a particular side.
     * If the tunnel already is registered, this will report a failure.
     *
     * @param tunnelPos The position of the tunnel inside the room.
     * @param type      The type of tunnel being registered.
     * @param machineId The machine the tunnel is to be connected to.
     * @param side      The side of the machine the tunnel is connecting to.
     * @return True if the connection could be established; false if the tunnel type is already registered for the given side.
     */
    public boolean registerTunnel(BlockPos tunnelPos, TunnelDefinition type, int machineId, Direction side) {
        // First we need to get the machine the tunnel is trying to connect to
        var machineNode = getOrCreateMachineNode(machineId);

        TunnelNode tunnelNode = getOrCreateTunnelNode(tunnelPos);
        if (graph.hasEdgeConnecting(tunnelNode, machineNode)) {
            // connection already formed between the tunnel at pos and the machine
            graph.edgeValue(tunnelNode, machineNode).ifPresent(edge -> {
                CompactMachines.LOGGER.info("Tunnel already registered for machine {} at position {}.",
                        machineId,
                        tunnelPos);
            });

            return false;
        }

        // graph direction is (tunnel)-[connected_to]->(machine)
        var tunnelsForSide = getTunnelsForSide(machineId, side).collect(Collectors.toSet());

        var tunnelTypeNode = getOrCreateTunnelTypeNode(type);

        // If tunnels are registered for the requested side, make sure there isn't a type conflict
        if (!tunnelsForSide.isEmpty()) {
            for (var sidedTunnel : tunnelsForSide) {
                // if we already have a tunnel with the same side and type, log the conflict and early exit
                var existingConn = graph.edgeValue(sidedTunnel, tunnelTypeNode);
                if (existingConn.isPresent()) {
                    CompactMachines.LOGGER.info("Tunnel type {} already registered for side {} at position {}.", type.getRegistryName(),
                            side.getSerializedName(),
                            sidedTunnel.position());

                    return false;
                }
            }
        }

        // no tunnels registered for side yet - free to make new tunnel node
        createTunnelAndLink(tunnelNode, side, machineNode, tunnelTypeNode);
        return true;
    }

    private void createTunnelAndLink(TunnelNode newTunnel, Direction side, CompactMachineNode machNode, TunnelTypeNode typeNode) {
        var newEdge = new TunnelMachineEdge(side);
        graph.putEdgeValue(newTunnel, machNode, newEdge);
        graph.putEdgeValue(newTunnel, typeNode, new TunnelTypeEdge());
    }

    @Nonnull
    public TunnelNode getOrCreateTunnelNode(BlockPos tunnelPos) {
        if (tunnels.containsKey(tunnelPos))
            return tunnels.get(tunnelPos);

        var newTunnel = new TunnelNode(tunnelPos);
        graph.addNode(newTunnel);
        tunnels.put(tunnelPos, newTunnel);
        return newTunnel;
    }

    public CompactMachineNode getOrCreateMachineNode(int machineId) {
        var machineRegistered = machines.containsKey(machineId);
        CompactMachineNode node;
        if (!machineRegistered) {
            node = new CompactMachineNode(machineId);
            machines.put(machineId, node);
            graph.addNode(node);
        } else {
            node = machines.get(machineId);
        }

        return node;
    }

    public TunnelTypeNode getOrCreateTunnelTypeNode(TunnelDefinition definition) {
        final ResourceLocation id = definition.getRegistryName();

        if (tunnelTypes.containsKey(id))
            return tunnelTypes.get(id);

        TunnelTypeNode newType = new TunnelTypeNode(id);
        graph.addNode(newType);
        tunnelTypes.put(id, newType);
        return newType;
    }

    /**
     * Gets the number of registered nodes in the graph.
     */
    public int size() {
        return graph.nodes().size();
    }

    public Stream<TunnelNode> getTunnelNodesByType(TunnelDefinition type) {
        var defNode = tunnelTypes.get(type.getRegistryName());
        if (defNode == null)
            return Stream.empty();

        return graph.predecessors(defNode)
                .stream()
                .filter(n -> n instanceof TunnelNode)
                .map(TunnelNode.class::cast);
    }

    public Set<BlockPos> getTunnelsByType(TunnelDefinition type) {
        return getTunnelNodesByType(type)
                .map(TunnelNode::position)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());
    }

    public Optional<Direction> getTunnelSide(BlockPos pos) {
        if (!tunnels.containsKey(pos))
            return Optional.empty();

        var node = tunnels.get(pos);
        return graph.successors(node).stream()
                .filter(outNode -> outNode instanceof CompactMachineNode)
                .map(mn -> graph.edgeValue(node, mn))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(TunnelMachineEdge.class::cast)
                .map(TunnelMachineEdge::side)
                .findFirst();
    }


    public Stream<IGraphNode> nodes() {
        return graph.nodes().stream();
    }

    @Override
    public CompoundTag serializeNBT() {
        cleanupOrphans();

        CompoundTag tag = new CompoundTag();

        HashMap<IGraphNode, UUID> nodeIds = new HashMap<>();

        var nodeList = nodes().map(node -> {
            CompoundTag nodeInfo = new CompoundTag();

            var codec = node.codec();
            var encoded = codec.encodeStart(NbtOps.INSTANCE, node);
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
            var codec = realEdge.codec();

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
        // Early exit if there are no nodes - no data to load
        if (!tag.contains("nodes"))
            return;

        final var nodes = tag.getList("nodes", Tag.TAG_COMPOUND);
        HashMap<UUID, IGraphNode> nodeMap = new HashMap<>(nodes.size());

        for (var nodeNbt : nodes) {
            if (!(nodeNbt instanceof CompoundTag nt))
                continue;

            if (!nt.contains("data") || !nt.hasUUID("id"))
                continue;

            UUID nodeId = nt.getUUID("id");
            CompoundTag nodeData = nt.getCompound("data");

            ResourceLocation nodeType = new ResourceLocation(nodeData.getString("type"));
            Codec<? extends IGraphNode> codec = CompactGraphs.getCodecForNode(nodeType);
            if (codec == null) continue;

            var res = codec.parse(NbtOps.INSTANCE, nodeData);

            try {
                final IGraphNode node = res.getOrThrow(false, CompactMachines.LOGGER::error);
                if (node instanceof CompactMachineNode m) {
                    final var mn = getOrCreateMachineNode(m.machineId());
                    nodeMap.putIfAbsent(nodeId, mn);
                }

                if (node instanceof TunnelNode t) {
                    final var tn = getOrCreateTunnelNode(t.position());
                    nodeMap.putIfAbsent(nodeId, tn);
                }

                if (node instanceof TunnelTypeNode tt) {
                    final var ttn = getOrCreateTunnelTypeNode(Tunnels.getDefinition(tt.id()));
                    nodeMap.putIfAbsent(nodeId, ttn);
                }

            } catch (RuntimeException ignored) {
            }
        }

        // No edges - skip rest of processing
        if (!tag.contains("edges"))
            return;

        final var edgeTags = tag.getList("edges", Tag.TAG_COMPOUND);
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

            var edgeCodec = CompactGraphs.getCodecForEdge(new ResourceLocation(edge.getCompound("data").getString("type")));
            if (edgeCodec == null)
                continue;

            var edgeData = edgeCodec.parse(NbtOps.INSTANCE, edge.getCompound("data"))
                    .getOrThrow(false, CompactMachines.LOGGER::error);

            graph.putEdgeValue(nodeFrom, nodeTo, edgeData);
        }
    }

    public boolean hasTunnel(BlockPos zero) {
        return tunnels.containsKey(zero);
    }

    public <T> Stream<BlockPos> getTunnelsSupporting(int machine, Direction side, Capability<T> capability) {
        final IGraphNode node = machines.get(machine);
        if (node == null) return Stream.empty();

        return getTunnelsForSide(machine, side)
                .filter(sided -> {
                    return graph.successors(sided).stream()
                            .filter(TunnelTypeNode.class::isInstance)
                            .map(TunnelTypeNode.class::cast)
                            .anyMatch(ttn -> {
                                var def = Tunnels.getDefinition(ttn.id());
                                if (!(def instanceof CapabilityTunnel<?> tcp))
                                    return false;

                                return tcp.getSupportedCapabilities().contains(capability);
                            });
                }).map(TunnelNode::position);
    }

    public Stream<TunnelDefinition> getTypesForSide(int machine, Direction side) {
        final IGraphNode node = machines.get(machine);
        if (node == null) return Stream.empty();

        return getTunnelsForSide(machine, side)
                .flatMap(tn -> graph.successors(tn).stream())
                .filter(TunnelTypeNode.class::isInstance)
                .map(TunnelTypeNode.class::cast)
                .map(type -> Tunnels.getDefinition(type.id()))
                .distinct();
    }

    public Stream<TunnelNode> getTunnelsForSide(int machine, Direction side) {
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

    public Stream<Direction> getTunnelSides(TunnelDefinition type) {
        if (!tunnelTypes.containsKey(type.getRegistryName()))
            return Stream.empty();

        return getTunnelsByType(type).stream()
                .map(this::getTunnelSide)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    public void deleteMachine(int machine) {
        if (!machines.containsKey(machine)) return;

        final var node = machines.get(machine);

        // Remove all connected tunnels
        graph.predecessors(node).stream()
                .filter(TunnelNode.class::isInstance)
                .map(TunnelNode.class::cast)
                .forEach(node1 -> {
                    var p = node1.position();
                    tunnels.remove(p);
                    graph.removeNode(node1);
                });

        graph.removeNode(node);
        machines.remove(machine);
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


    public Stream<BlockPos> getMachineTunnels(int machine, TunnelDefinition type) {
        return getTunnelNodesByType(type)
                .map(TunnelNode::position)
                .filter(position -> connectedMachine(position).map(m -> m == machine).orElse(false))
                .map(BlockPos::immutable);
    }

    public void unregister(BlockPos pos) {
        if (!hasTunnel(pos))
            return;

        final var existing = tunnels.get(pos);
        graph.removeNode(existing);
        tunnels.remove(pos);

        cleanupOrphanedTypes();
        cleanupOrphanedMachines();
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

        removedTypes.forEach(tunnelTypes::remove);
    }

    private void cleanupOrphanedTunnels() {
        HashSet<BlockPos> removed = new HashSet<>();
        tunnels.forEach((pos, node) -> {
            if (graph.degree(node) == 0) {
                graph.removeNode(node);
                removed.add(pos);
            }
        });

        removed.forEach(tunnels::remove);
    }

    private void cleanupOrphanedMachines() {
        HashSet<Integer> removed = new HashSet<>();
        machines.forEach((machine, node) -> {
            if (graph.degree(node) == 0) {
                graph.removeNode(node);
                removed.add(machine);
            }
        });

        removed.forEach(machines::remove);
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
        });
    }

    public Stream<Integer> getMachines() {
        return this.machines.keySet().stream();
    }

    public Stream<BlockPos> getConnections(int machineId) {
        if (!machines.containsKey(machineId))
            return Stream.empty();

        final var mNode = machines.get(machineId);
        return graph.incidentEdges(mNode).stream()
                .filter(e -> graph.edgeValue(e).orElseThrow() instanceof MachineLinkEdge)
                .map(edge -> {
                    if (edge.nodeU() instanceof TunnelNode cmn) return cmn;
                    if (edge.nodeV() instanceof TunnelNode cmn2) return cmn2;
                    return null;
                }).filter(Objects::nonNull).map(TunnelNode::position);

    }

    public boolean hasAnyConnectedTo(int machineId) {
        return getConnections(machineId).findAny().isPresent();
    }
}
