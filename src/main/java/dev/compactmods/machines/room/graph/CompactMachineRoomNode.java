package dev.compactmods.machines.room.graph;

import java.util.Objects;
import dev.compactmods.machines.graph.IGraphNode;
import net.minecraft.world.level.ChunkPos;

/**
 * Represents the inside of a Compact Machine.
 */
public record CompactMachineRoomNode(ChunkPos pos) implements IGraphNode {

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
}
