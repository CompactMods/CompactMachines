package dev.compactmods.machines.graph;

import dev.compactmods.machines.core.Registries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class Graph {

    public static final Supplier<IForgeRegistry<IGraphNodeType>> NODE_TYPE_REG = Registries.NODE_TYPES.makeRegistry(RegistryBuilder::new);
    public static final Supplier<IForgeRegistry<IGraphEdgeType>> EDGE_TYPE_REG = Registries.EDGE_TYPES.makeRegistry(RegistryBuilder::new);


    public static final RegistryObject<IGraphNodeType> MACH_NODE = Registries.NODE_TYPES.register("machine", () -> GraphNodeType.MACHINE);
    public static final RegistryObject<IGraphNodeType> DIM_NODE = Registries.NODE_TYPES.register("dimension", () -> GraphNodeType.DIMENSION);
    public static final RegistryObject<IGraphNodeType> ROOM_NODE = Registries.NODE_TYPES.register("room", () -> GraphNodeType.ROOM);

    public static final RegistryObject<IGraphNodeType> TUNNEL_NODE = Registries.NODE_TYPES.register("tunnel", () -> GraphNodeType.TUNNEL);
    public static final RegistryObject<IGraphNodeType> TUNNEL_TYPE_NODE = Registries.NODE_TYPES.register("tunnel_type", () -> GraphNodeType.TUNNEL_TYPE);

    public static final RegistryObject<IGraphNodeType> ROOM_UPGRADE_NODE = Registries.NODE_TYPES.register("room_upgrade", () -> GraphNodeType.ROOM_UPGRADE);

    public static final RegistryObject<IGraphEdgeType> MACHINE_LINK = Registries.EDGE_TYPES.register("machine_link", () -> GraphEdgeType.MACHINE_LINK);

    // Tunnel edges
    public static final RegistryObject<IGraphEdgeType> TUNNEL_TYPE = Registries.EDGE_TYPES.register("tunnel_type", () -> GraphEdgeType.TUNNEL_TYPE);
    public static final RegistryObject<IGraphEdgeType> TUNNEL_MACHINE_LINK = Registries.EDGE_TYPES.register("tunnel_machine", () -> GraphEdgeType.TUNNEL_MACHINE);

    public static void prepare() {

    }
}
