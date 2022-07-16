package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;

public interface IGraphNodeType {

    <D extends IGraphNode> Codec<D> codec();
}
