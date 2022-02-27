package dev.compactmods.machines.tunnel.graph;

import dev.compactmods.machines.graph.IGraphEdge;
import net.minecraft.core.Direction;

public record TunnelMachineEdge(Direction side) implements IGraphEdge {
}
