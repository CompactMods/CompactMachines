package dev.compactmods.machines.machine.graph;

import java.util.Objects;
import dev.compactmods.machines.graph.IGraphNode;

/**
 * Represents a machine's external point. This can be either inside a machine or in a dimension somewhere.
 */
public record CompactMachineNode(int machineId) implements IGraphNode {

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
}
