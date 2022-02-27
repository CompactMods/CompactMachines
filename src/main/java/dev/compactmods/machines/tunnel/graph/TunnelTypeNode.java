package dev.compactmods.machines.tunnel.graph;

import dev.compactmods.machines.graph.IGraphNode;
import net.minecraft.resources.ResourceLocation;

public record TunnelTypeNode(ResourceLocation id) implements IGraphNode {
}
