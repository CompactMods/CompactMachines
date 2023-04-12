package dev.compactmods.machines.forge.tunnel.graph;

import com.google.common.graph.EndpointPair;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphNode;

@SuppressWarnings("UnstableApiUsage")
public record GraphEdgeLookupResult<E extends IGraphEdge<E>>(EndpointPair<IGraphNode<?>> endpoints, E edgeValue) {
}
