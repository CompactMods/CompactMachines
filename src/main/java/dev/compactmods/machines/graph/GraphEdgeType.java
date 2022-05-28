package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.machine.graph.MachineRoomEdge;
import dev.compactmods.machines.tunnel.graph.TunnelMachineEdge;
import dev.compactmods.machines.tunnel.graph.TunnelTypeEdge;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public enum GraphEdgeType implements IGraphEdgeType {
    TUNNEL_TYPE(TunnelTypeEdge.CODEC),
    MACHINE_LINK(MachineRoomEdge.CODEC),
    TUNNEL_MACHINE(TunnelMachineEdge.CODEC);

    private final Codec<IGraphEdge> codec;
    private ResourceLocation regName;

    @SuppressWarnings("unchecked")
    GraphEdgeType(Codec<? extends IGraphEdge> codec) {
        this.codec = (Codec<IGraphEdge>) codec;
    }

    @Override
    public Codec<IGraphEdge> codec() {
        return codec;
    }

    @Override
    public IGraphEdgeType setRegistryName(ResourceLocation name) {
        this.regName = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return regName;
    }

    @Override
    public Class<IGraphEdgeType> getRegistryType() {
        return CMGraphRegistration.EDGE_TYPE_REG.get().getRegistrySuperType();
    }
}
