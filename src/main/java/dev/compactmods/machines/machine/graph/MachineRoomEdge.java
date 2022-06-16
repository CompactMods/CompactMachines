package dev.compactmods.machines.machine.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.graph.CMGraphRegistration;
import dev.compactmods.machines.graph.GraphEdgeType;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphEdgeType;
import org.jetbrains.annotations.NotNull;

public record MachineRoomEdge() implements IGraphEdge {

    public static final Codec<IGraphEdge> CODEC = Codec.unit(MachineRoomEdge::new);

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

    @Override
    public @NotNull IGraphEdgeType getEdgeType() {
        return GraphEdgeType.MACHINE_LINK;
    }
}
