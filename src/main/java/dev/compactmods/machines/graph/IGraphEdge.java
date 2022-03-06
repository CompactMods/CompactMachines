package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;

public interface IGraphEdge {
    <T extends IGraphEdge> Codec<T> codec();
}
