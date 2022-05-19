package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.IForgeRegistryEntry;

public interface IGraphEdgeType extends IForgeRegistryEntry<IGraphEdgeType> {
    Codec<IGraphEdge> codec();
}
