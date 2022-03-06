package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;

public interface IGraphNode {

    <T extends IGraphNode> Codec<T> codec();
}
