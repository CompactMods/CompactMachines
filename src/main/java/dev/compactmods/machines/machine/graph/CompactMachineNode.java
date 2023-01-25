package dev.compactmods.machines.machine.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.CMGraphRegistration;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Represents a machine's external point. This can be either inside a machine or in a dimension somewhere.
 */
public record CompactMachineNode(ResourceKey<Level> dimension, BlockPos position)
        implements IGraphNode {

    public static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "machine");

    public static final Codec<CompactMachineNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(CompactMachineNode::dimension),
            BlockPos.CODEC.fieldOf("position").forGetter(CompactMachineNode::position),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (dim, pos, type) -> new CompactMachineNode(dim, pos)));

    public String toString() {
        return "Compact Machine {%s}".formatted(position);
    }

    public GlobalPos dimpos() {
        return GlobalPos.of(dimension, position);
    }

    @Override
    public IGraphNodeType getType() {
        return CMGraphRegistration.MACH_NODE.get();
    }
}
