package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Objects;

public final class DimensionGraphNode extends GraphNodeBase implements IGraphNodeType {

    private static final Codec<DimensionGraphNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).fieldOf("dim").forGetter(DimensionGraphNode::dimension)
    ).apply(i, DimensionGraphNode::new));
    private final ResourceKey<Level> dimension;

    public DimensionGraphNode() {
        this.dimension = null;
    }

    public DimensionGraphNode(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    @Override
    public Codec<DimensionGraphNode> codec() {
        return CODEC;
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DimensionGraphNode) obj;
        return Objects.equals(this.dimension, that.dimension);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension);
    }

    @Override
    public String toString() {
        return "DimensionGraphNode[" +
                "dimension=" + dimension + ']';
    }

    @Override
    public IGraphNodeType getType() {
        return CMGraphRegistration.DIM_NODE.get();
    }
}
