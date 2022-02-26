package dev.compactmods.machines.tunnel.graph;

import net.minecraft.core.BlockPos;

public record TunnelNode(BlockPos position) implements ITunnelGraphNode {
}
