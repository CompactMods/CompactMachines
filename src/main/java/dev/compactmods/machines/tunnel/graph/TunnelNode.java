package dev.compactmods.machines.tunnel.graph;

import dev.compactmods.machines.graph.IGraphNode;
import net.minecraft.core.BlockPos;

public record TunnelNode(BlockPos position) implements IGraphNode {
}
