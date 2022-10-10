package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;

import java.util.function.Supplier;

public class SimpleGraphNodeType<T extends IGraphNode> implements IGraphNodeType<T> {

    private final Codec<T> codec;

    private SimpleGraphNodeType(Codec<T> codec) {
        this.codec = codec;
    }

    public static <T extends IGraphNode> Supplier<SimpleGraphNodeType<T>> instance(Codec<T> codec) {
        return () -> new SimpleGraphNodeType<T>(codec);
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }
}
