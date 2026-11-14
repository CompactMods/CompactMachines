package dev.compactmods.machines.room.graph.node;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.feather.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents the inside of a Compact Machine.
 */
public record RoomReferenceNode(UUID id, String code) implements Node<String> {

    public static final Codec<RoomReferenceNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("roomCode").forGetter(RoomReferenceNode::code)
    ).apply(i, RoomReferenceNode::new));

    public RoomReferenceNode(String code) {
        this(UUID.randomUUID(), code);
    }

    public @NotNull Codec<RoomReferenceNode> codec() {
        return CODEC;
    }

    @Override
    public String data() {
        return code;
    }
}
