package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;

public interface IGraphEdgeType {
    Codec<IGraphEdge> codec();
}
