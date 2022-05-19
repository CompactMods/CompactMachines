package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IGraphNodeType extends IForgeRegistryEntry<IGraphNodeType> {

    <D extends IGraphNode> Codec<D> codec();
}
