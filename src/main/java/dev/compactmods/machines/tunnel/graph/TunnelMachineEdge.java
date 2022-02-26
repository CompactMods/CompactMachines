package dev.compactmods.machines.tunnel.graph;

import net.minecraft.core.Direction;

public record TunnelMachineEdge(Direction side) implements ITunnelGraphEdge {
}
