package dev.compactmods.machines.graph;

import dev.compactmods.machines.core.Registries;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.room.graph.RoomReferenceNode;
import dev.compactmods.machines.upgrade.graph.RoomUpgradeGraphNode;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class Graph {

    public static final Supplier<IForgeRegistry<IGraphNodeType<?>>> NODE_TYPE_REG = Registries.NODE_TYPES.makeRegistry(RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<IGraphEdgeType<?>>> EDGE_TYPE_REG = Registries.EDGE_TYPES.makeRegistry(RegistryBuilder::new);


    public static final RegistryObject<IGraphNodeType<CompactMachineNode>> MACH_NODE = Registries.NODE_TYPES
            .register("dev/compactmods/machines/api/machine", SimpleGraphNodeType.instance(CompactMachineNode.CODEC));
    public static final RegistryObject<IGraphNodeType<DimensionGraphNode>> DIM_NODE = Registries.NODE_TYPES
            .register("dev/compactmods/machines/api/dimension", SimpleGraphNodeType.instance(DimensionGraphNode.CODEC));
    public static final RegistryObject<IGraphNodeType<RoomReferenceNode>> ROOM_REFERENCE_NODE = Registries.NODE_TYPES
            .register("dev/compactmods/machines/api/room", SimpleGraphNodeType.instance(RoomReferenceNode.CODEC));

    public static final RegistryObject<IGraphNodeType<RoomUpgradeGraphNode>> ROOM_UPGRADE_NODE = Registries.NODE_TYPES
            .register("room_upgrade", SimpleGraphNodeType.instance(RoomUpgradeGraphNode.CODEC));




    public static final RegistryObject<IGraphEdgeType<IGraphEdge>> MACHINE_LINK = Registries.EDGE_TYPES.register("machine_link", () -> GraphEdgeType.MACHINE_LINK);

    // Tunnel edges
    public static final RegistryObject<IGraphEdgeType<IGraphEdge>> TUNNEL_TYPE = Registries.EDGE_TYPES.register("tunnel_type", () -> GraphEdgeType.TUNNEL_TYPE);
    public static final RegistryObject<IGraphEdgeType<IGraphEdge>> TUNNEL_MACHINE_LINK = Registries.EDGE_TYPES.register("tunnel_machine", () -> GraphEdgeType.TUNNEL_MACHINE);

    public static void prepare() {

    }
}
