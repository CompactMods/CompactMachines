package dev.compactmods.machines.machine.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.CMGraphRegistration;
import dev.compactmods.machines.graph.GraphNodeBase;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.location.LevelBlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * Represents a machine's external point. This can be either inside a machine or in a dimension somewhere.
 */
public final class CompactMachineNode extends GraphNodeBase implements IGraphNodeType {

    public static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "machine");

    public static final Codec<CompactMachineNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(CompactMachineNode::dimension),
            BlockPos.CODEC.fieldOf("position").forGetter(CompactMachineNode::position),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (dim, pos, type) -> new CompactMachineNode(dim, pos)));
    private final ResourceKey<Level> dimension;
    private final BlockPos position;

    public CompactMachineNode() {
        this.dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(CompactMachines.MOD_ID, "empty"));
        this.position = BlockPos.ZERO;
    }

    /**
     */
    public CompactMachineNode(ResourceKey<Level> dimension, BlockPos position) {
        this.dimension = dimension;
        this.position = position;
    }

    public String toString() {
        return "Compact Machine {%s}".formatted(position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompactMachineNode that = (CompactMachineNode) o;
        return position.equals(that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    @Override
    public Codec<CompactMachineNode> codec() {
        return CODEC;
    }

    public LevelBlockPosition dimpos() {
        return new LevelBlockPosition(dimension, position);
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public BlockPos position() {
        return position;
    }

    @Override
    public IGraphNodeType getType() {
        return CMGraphRegistration.MACH_NODE.get();
    }
}
