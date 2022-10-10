package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;

public interface IGraphEdgeType<T extends IGraphEdge> {
    Codec<T> codec();
}
