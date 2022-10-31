package dev.compactmods.machines.machine.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.graph.Graph;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.location.LevelBlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Represents a machine's external point. This can be either inside a machine or in a dimension somewhere.
 */
public record CompactMachineNode(ResourceKey<Level> dimension, BlockPos position)
        implements IGraphNode {

    public static final ResourceLocation TYPE = new ResourceLocation(Constants.MOD_ID, "dev/compactmods/machines/api/machine");

    public static final Codec<CompactMachineNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dev/compactmods/machines/api/dimension").forGetter(CompactMachineNode::dimension),
            BlockPos.CODEC.fieldOf("position").forGetter(CompactMachineNode::position),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (dim, pos, type) -> new CompactMachineNode(dim, pos)));

    public String toString() {
        return "Compact Machine {%s}".formatted(position);
    }

    public LevelBlockPosition dimpos() {
        return new LevelBlockPosition(dimension, position);
    }

    @Override
    public IGraphNodeType getType() {
        return Graph.MACH_NODE.get();
    }
}
