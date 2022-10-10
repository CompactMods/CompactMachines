package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;

import java.util.function.Supplier;

public class SimpleGraphEdgeType<T extends IGraphEdge> implements IGraphEdgeType<T> {
    private final Codec<T> codec;

    private SimpleGraphEdgeType(Codec<T> codec) {
        this.codec = codec;
    }

    public static <T extends IGraphEdge> Supplier<SimpleGraphEdgeType<T>> instance(Codec<T> codec) {
        return () -> new SimpleGraphEdgeType<T>(codec);
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }
}
