package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record DimensionGraphNode(ResourceKey<Level> dimension) implements IGraphNode<DimensionGraphNode> {

    public static final Codec<DimensionGraphNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).fieldOf("dim").forGetter(DimensionGraphNode::dimension)
    ).apply(i, DimensionGraphNode::new));

    public static final IGraphNodeType<DimensionGraphNode> NODE_TYPE = SimpleGraphNodeType.instance(CODEC);

    @Override
    public String toString() {
        return "DimensionGraphNode[%s]".formatted(dimension);
    }

    @Override
    public IGraphNodeType<DimensionGraphNode> getType() {
        return NODE_TYPE;
    }
}
