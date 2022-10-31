package dev.compactmods.machines.room.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public record RoomOwnerNode(UUID owner) implements IGraphNode<RoomOwnerNode> {

    private static final ResourceLocation TYPE = new ResourceLocation(Constants.MOD_ID, "dev/compactmods/machines/api/room");

    public static final Codec<RoomOwnerNode> CODEC = RecordCodecBuilder.create((i) -> i.group(
            UUIDUtil.CODEC.fieldOf("owner").forGetter(RoomOwnerNode::owner),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> RoomOwnerNode.TYPE)
    ).apply(i, (owner, type) -> new RoomOwnerNode(owner)));

    @Override
    public IGraphNodeType<RoomOwnerNode> getType() {
        return Rooms.ROOM_OWNER_NODE.get();
    }
}
