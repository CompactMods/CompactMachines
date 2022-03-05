package dev.compactmods.machines.tunnel.graph;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a room's tunnel connections in a graph-style format.
 * This should be accessed through the saved data for specific machine room chunks.
 */
public class TunnelConnectionGraph {

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
                .findFirst()
                .filter(n -> n instanceof CompactMachineNode)
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
        var machineRegistered = machines.containsKey(machineId);
        CompactMachineNode CompactMachineNode;
        if (!machineRegistered) {
            CompactMachineNode = new CompactMachineNode(machineId);
            machines.put(machineId, CompactMachineNode);
            graph.addNode(CompactMachineNode);
        } else {
            CompactMachineNode = machines.get(machineId);
        }

        TunnelNode tunnelNode = getOrCreateTunnelNode(tunnelPos);
        if(graph.hasEdgeConnecting(tunnelNode, CompactMachineNode)) {
            // connection already formed between the tunnel at pos and the machine
            graph.edgeValue(tunnelNode, CompactMachineNode).ifPresent(edge -> {
                CompactMachines.LOGGER.info("Tunnel already registered for machine {} at position {}.",
                        machineId,
                        tunnelPos);
            });

            return false;
        }

        // graph direction is (tunnel)-[connected_to]->(machine)
        var tunnelsForSide = graph.predecessors(CompactMachineNode)
                .stream()
                .filter(n -> n instanceof TunnelNode)
                .filter(tunnel -> graph.edgeValue(CompactMachineNode, tunnel)
                        .map(edge -> !((TunnelMachineEdge) edge).side().equals(side))
                        .orElse(false))
                .map(t -> (TunnelNode) t)
                .collect(Collectors.toSet());

        var tunnelTypeNode = createOrRegisterTunnelType(type);

        // If tunnels are registered for the requested side, make sure there isn't a type conflict
        if (!tunnelsForSide.isEmpty()) {
            for (var sidedTunnel : tunnelsForSide) {
                // if we already have a tunnel with the same side and type, log the conflict and early exit
                var existingConn = graph.edgeValue(sidedTunnel, tunnelTypeNode);
                if(existingConn.isPresent()) {
                    CompactMachines.LOGGER.info("Tunnel type {} already registered for side {} at position {}.", type.getRegistryName(),
                            side.getSerializedName(),
                            sidedTunnel.position());

                    return false;
                }
            }
        }

        // no tunnels registered for side yet - free to make new tunnel node
        createTunnelAndLink(tunnelNode, side, CompactMachineNode, tunnelTypeNode);
        return true;
    }

    private void createTunnelAndLink(TunnelNode newTunnel, Direction side, CompactMachineNode CompactMachineNode, TunnelTypeNode typeNode) {
        var newEdge = new TunnelMachineEdge(side);
        graph.putEdgeValue(newTunnel, CompactMachineNode, newEdge);
        graph.putEdgeValue(newTunnel, typeNode, new TunnelTypeEdge());
    }

    @Nonnull
    private TunnelNode getOrCreateTunnelNode(BlockPos tunnelPos) {
        if(tunnels.containsKey(tunnelPos))
            return tunnels.get(tunnelPos);

        var newTunnel = new TunnelNode(tunnelPos);
        graph.addNode(newTunnel);
        tunnels.put(tunnelPos, newTunnel);
        return newTunnel;
    }

    public TunnelTypeNode createOrRegisterTunnelType(TunnelDefinition definition) {
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

    public Set<BlockPos> getTunnels(TunnelDefinition type) {
        var defNode = tunnelTypes.get(type.getRegistryName());
        if(defNode == null)
            return Collections.emptySet();

        return graph.predecessors(defNode)
                .stream()
                .filter(n -> n instanceof TunnelNode)
                .map(TunnelNode.class::cast)
                .map(TunnelNode::position)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());
    }

    public Optional<Direction> getTunnelSide(BlockPos pos) {
        if(!tunnels.containsKey(pos))
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
}
