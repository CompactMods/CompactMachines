package dev.compactmods.machines;

import dev.compactmods.machines.graph.DimensionGraphNode;
import dev.compactmods.machines.graph.SimpleGraphNodeType;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.machine.graph.MachineRoomEdge;
import dev.compactmods.machines.room.graph.RoomReferenceNode;
import dev.compactmods.machines.room.upgrade.graph.RoomUpgradeGraphNode;

public class GraphCommon {

    static {
        Registries.NODE_TYPES.register("machine", SimpleGraphNodeType.supplier(CompactMachineNode.CODEC));
        Registries.NODE_TYPES.register("dimension", SimpleGraphNodeType.supplier(DimensionGraphNode.CODEC));
        Registries.NODE_TYPES.register("room", SimpleGraphNodeType.supplier(RoomReferenceNode.CODEC));
        Registries.NODE_TYPES.register("room_upgrade", SimpleGraphNodeType.supplier(RoomUpgradeGraphNode.CODEC));

        Registries.EDGE_TYPES.register("machine_link", () -> MachineRoomEdge.EDGE_TYPE);
    }

    public static void prepare() {

    }
}
