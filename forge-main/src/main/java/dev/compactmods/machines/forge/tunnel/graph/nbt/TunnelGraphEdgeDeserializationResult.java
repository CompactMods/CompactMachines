package dev.compactmods.machines.forge.tunnel.graph.nbt;

import dev.compactmods.machines.graph.IGraphEdge;

import java.util.UUID;

public record TunnelGraphEdgeDeserializationResult<E extends IGraphEdge<E>>(UUID from, UUID to, E data) {
}
