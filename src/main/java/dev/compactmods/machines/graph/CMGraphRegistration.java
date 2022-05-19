package dev.compactmods.machines.graph;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.room.graph.CompactMachineRoomNode;
import dev.compactmods.machines.tunnel.graph.TunnelNode;
import dev.compactmods.machines.tunnel.graph.TunnelTypeNode;
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
    public static final Supplier<IForgeRegistry<IGraphNodeType>> NODE_TYPE_REG = NODE_TYPES.makeRegistry(IGraphNodeType.class,
            () -> new RegistryBuilder<IGraphNodeType>().setName(NODES_RL));

    public static final ResourceLocation EDGES_RL = new ResourceLocation(CompactMachines.MOD_ID, "graph_edges");
    public static final DeferredRegister<IGraphEdgeType> EDGE_TYPES = DeferredRegister.create(EDGES_RL, CompactMachines.MOD_ID);
    public static final Supplier<IForgeRegistry<IGraphEdgeType>> EDGE_TYPE_REG = EDGE_TYPES.makeRegistry(IGraphEdgeType.class,
            () -> new RegistryBuilder<IGraphEdgeType>().setName(EDGES_RL));


    public static final RegistryObject<CompactMachineNode> MACH_NODE = NODE_TYPES.register("machine", CompactMachineNode::new);
    public static final RegistryObject<DimensionGraphNode> DIM_NODE = NODE_TYPES.register("dimension", DimensionGraphNode::new);
    public static final RegistryObject<CompactMachineRoomNode> ROOM_NODE = NODE_TYPES.register("room", CompactMachineRoomNode::new);
    public static final RegistryObject<TunnelNode> TUNNEL_NODE = NODE_TYPES.register("tunnel", TunnelNode::new);
    public static final RegistryObject<TunnelTypeNode> TUNNEL_TYPE_NODE = NODE_TYPES.register("tunnel_type", TunnelTypeNode::new);

    public static final RegistryObject<IGraphEdgeType> MACHINE_LINK = EDGE_TYPES.register("machine_link", () -> GraphEdgeType.MACHINE_LINK);
    public static final RegistryObject<IGraphEdgeType> TUNNEL_TYPE = EDGE_TYPES.register("tunnel_type", () -> GraphEdgeType.TUNNEL_TYPE);

    public static void init(IEventBus bus) {
        NODE_TYPES.register(bus);
        EDGE_TYPES.register(bus);
    }
}
