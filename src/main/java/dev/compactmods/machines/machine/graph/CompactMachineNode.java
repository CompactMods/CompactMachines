package dev.compactmods.machines.machine.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.IGraphNode;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * Represents a machine's external point. This can be either inside a machine or in a dimension somewhere.
 */
public record CompactMachineNode(int machineId) implements IGraphNode {

    public static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "machine");

    public static final Codec<CompactMachineNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("machine").forGetter(CompactMachineNode::machineId),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (id, type) -> new CompactMachineNode(id)));

    public String label() {
        return "Compact Machine #" + machineId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompactMachineNode that = (CompactMachineNode) o;
        return machineId == that.machineId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(machineId);
    }

    @Override
    public Codec<CompactMachineNode> codec() {
        return CODEC;
    }
}
