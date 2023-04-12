package dev.compactmods.machines.forge.tunnel.graph.nbt;

import dev.compactmods.machines.graph.IGraphNode;
import net.minecraft.nbt.ListTag;

import java.util.Map;
import java.util.UUID;

public record TunnelGraphNodeSerializationResult<T extends IGraphNode<?>>(Map<T, UUID> idMap, ListTag listTag) {}
