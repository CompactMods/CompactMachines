package dev.compactmods.machines.graph;

import dev.compactmods.machines.CompactMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class CMGraphRegistration {

    public static final ResourceLocation NODES_RL = new ResourceLocation(CompactMachines.MOD_ID, "graph_nodes");
    public static final DeferredRegister<IGraphNodeType> NODE_TYPES = DeferredRegister.create(NODES_RL, CompactMachines.MOD_ID);
    public static final Supplier<IForgeRegistry<IGraphNodeType>> NODE_TYPE_REG = NODE_TYPES.makeRegistry(
            () -> new RegistryBuilder<IGraphNodeType>().setName(NODES_RL));

    public static final ResourceLocation EDGES_RL = new ResourceLocation(CompactMachines.MOD_ID, "graph_edges");
    public static final DeferredRegister<IGraphEdgeType> EDGE_TYPES = DeferredRegister.create(EDGES_RL, CompactMachines.MOD_ID);
    public static final Supplier<IForgeRegistry<IGraphEdgeType>> EDGE_TYPE_REG = EDGE_TYPES.makeRegistry(
            () -> new RegistryBuilder<IGraphEdgeType>().setName(EDGES_RL));


    public static final RegistryObject<IGraphNodeType> MACH_NODE = NODE_TYPES.register("machine", () -> GraphNodeType.MACHINE);
    public static final RegistryObject<IGraphNodeType> DIM_NODE = NODE_TYPES.register("dimension", () -> GraphNodeType.DIMENSION);
    public static final RegistryObject<IGraphNodeType> ROOM_NODE = NODE_TYPES.register("room", () -> GraphNodeType.ROOM);

    public static final RegistryObject<IGraphNodeType> TUNNEL_NODE = NODE_TYPES.register("tunnel", () -> GraphNodeType.TUNNEL);
    public static final RegistryObject<IGraphNodeType> TUNNEL_TYPE_NODE = NODE_TYPES.register("tunnel_type", () -> GraphNodeType.TUNNEL_TYPE);

    public static final RegistryObject<IGraphNodeType> ROOM_UPGRADE_NODE = NODE_TYPES.register("room_upgrade", () -> GraphNodeType.ROOM_UPGRADE);

    public static final RegistryObject<IGraphEdgeType> MACHINE_LINK = EDGE_TYPES.register("machine_link", () -> GraphEdgeType.MACHINE_LINK);

    // Tunnel edges
    public static final RegistryObject<IGraphEdgeType> TUNNEL_TYPE = EDGE_TYPES.register("tunnel_type", () -> GraphEdgeType.TUNNEL_TYPE);
    public static final RegistryObject<IGraphEdgeType> TUNNEL_MACHINE_LINK = EDGE_TYPES.register("tunnel_machine", () -> GraphEdgeType.TUNNEL_MACHINE);

    public static void init(IEventBus bus) {
        NODE_TYPES.register(bus);
        EDGE_TYPES.register(bus);
    }
}
