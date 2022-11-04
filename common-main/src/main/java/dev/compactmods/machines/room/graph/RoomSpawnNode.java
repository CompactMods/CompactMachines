package dev.compactmods.machines.room.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.codec.CodecExtensions;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.graph.SimpleGraphNodeType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public record RoomSpawnNode(Vec3 position, Vec2 rotation) implements IGraphNode<RoomSpawnNode> {

    public static final Codec<RoomSpawnNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            Vec3.CODEC.fieldOf("position").forGetter(RoomSpawnNode::position),
            CodecExtensions.VEC2.fieldOf("rotation").forGetter(RoomSpawnNode::rotation)
    ).apply(i, RoomSpawnNode::new));

    public static final IGraphNodeType<RoomSpawnNode> NODE_TYPE = SimpleGraphNodeType.instance(CODEC);

    @Override
    public IGraphNodeType<RoomSpawnNode> getType() {
        return NODE_TYPE;
    }
}
