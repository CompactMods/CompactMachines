package dev.compactmods.machines.room.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.graph.Graph;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

/**
 * Represents the inside of a Compact Machine.
 */
public record CompactMachineRoomNode(ChunkPos pos) implements IGraphNode {

    private static final ResourceLocation TYPE = new ResourceLocation(Constants.MOD_ID, "room");

    public static final Codec<CompactMachineRoomNode> CODEC = RecordCodecBuilder.create((i) -> i.group(
            CodecExtensions.CHUNKPOS.fieldOf("chunk").forGetter(CompactMachineRoomNode::pos),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (pos, type) -> new CompactMachineRoomNode(pos)));

    @Override
    public String toString() {
        return "CompactMachineRoomNode[" +
                "pos=" + pos + ']';
    }

    @Override
    public IGraphNodeType getType() {
        return Graph.ROOM_NODE.get();
    }
}
