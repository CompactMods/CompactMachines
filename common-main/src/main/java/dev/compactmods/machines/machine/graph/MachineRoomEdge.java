package dev.compactmods.machines.machine.graph;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphEdgeType;
import dev.compactmods.machines.graph.SimpleGraphEdgeType;
import org.jetbrains.annotations.NotNull;

public record MachineRoomEdge() implements IGraphEdge<MachineRoomEdge> {

    public static final Codec<MachineRoomEdge> CODEC = Codec.unit(MachineRoomEdge::new);

    public static final IGraphEdgeType<MachineRoomEdge> EDGE_TYPE = SimpleGraphEdgeType.instance(CODEC);

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "MachineRoomEdge[]";
    }

    @NotNull
    @Override
    public IGraphEdgeType<MachineRoomEdge> getEdgeType() {
        return EDGE_TYPE;
    }
}
