package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;

import java.util.function.Supplier;

public class SimpleGraphEdgeType<T extends IGraphEdge> implements IGraphEdgeType<T> {
    private final Codec<T> codec;

    private SimpleGraphEdgeType(Codec<T> codec) {
        this.codec = codec;
    }

    public static <T extends IGraphEdge> SimpleGraphEdgeType<T> instance(Codec<T> codec) {
        return new SimpleGraphEdgeType<T>(codec);
    }

    public static <T extends IGraphEdge> Supplier<SimpleGraphEdgeType<T>> supplier(Codec<T> codec) {
        return () -> instance(codec);
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }
}
