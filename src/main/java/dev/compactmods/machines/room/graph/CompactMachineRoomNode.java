package dev.compactmods.machines.room.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.graph.IGraphNode;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;

/**
 * Represents the inside of a Compact Machine.
 */
public record CompactMachineRoomNode(ChunkPos pos) implements IGraphNode {

    private static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "room");

    public static final Codec<CompactMachineRoomNode> CODEC = RecordCodecBuilder.create((i) -> i.group(
            CodecExtensions.CHUNKPOS.fieldOf("chunk").forGetter(CompactMachineRoomNode::pos),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (pos, type) -> new CompactMachineRoomNode(pos)));

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompactMachineRoomNode that = (CompactMachineRoomNode) o;
        return pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos);
    }

    @Override
    public Codec<CompactMachineRoomNode> codec() {
        return CODEC;
    }
}
