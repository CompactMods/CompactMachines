package dev.compactmods.machines.room.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.graph.SimpleGraphNodeType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RoomOwnerNode(UUID owner) implements IGraphNode<RoomOwnerNode> {

    private static final ResourceLocation TYPE = new ResourceLocation(Constants.MOD_ID, "room");

    public static final Codec<RoomOwnerNode> CODEC = RecordCodecBuilder.create((i) -> i.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(RoomOwnerNode::owner),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> RoomOwnerNode.TYPE)
    ).apply(i, (owner, type) -> new RoomOwnerNode(owner)));

    public static final IGraphNodeType<RoomOwnerNode> NODE_TYPE = SimpleGraphNodeType.instance(CODEC);

    @Override
    public IGraphNodeType<RoomOwnerNode> getType() {
        return NODE_TYPE;
    }
}
