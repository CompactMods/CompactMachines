package com.robotgryphon.compactmachines.data.graph;

import com.google.common.graph.MutableGraph;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;

import java.util.Objects;

/**
 * Represents a machine's external point. This can be either inside a machine or in a dimension somewhere.
 */
public class CompactMachineNode implements IMachineGraphNode {
    public boolean isConnected;
    private int machineId;
    private IMachineGraphNode linkedTo;
    public MachineExternalLocation location;
    public DimensionalPosition position;
    private MutableGraph<IMachineGraphNode> graph;

    public CompactMachineNode(MutableGraph<IMachineGraphNode> graph, int machine) {
        this.graph = graph;
        this.machineId = machine;
        this.isConnected = false;
    }

    public void connectTo(IMachineGraphNode newInside) {
        if (this.linkedTo != null) {
            graph.removeEdge(this, linkedTo);
            this.isConnected = false;
        }

        this.linkedTo = newInside;
        this.isConnected = true;
        graph.putEdge(this, linkedTo);
    }

    @Override
    public String label() {
        return "Compact Machine #" + machineId;
    }

    @Override
    public String getId() {
        return "machine" + machineId;
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
