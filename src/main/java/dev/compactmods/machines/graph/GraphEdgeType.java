package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.machine.graph.MachineRoomEdge;
import dev.compactmods.machines.tunnel.graph.TunnelMachineEdge;
import dev.compactmods.machines.tunnel.graph.TunnelTypeEdge;
import dev.compactmods.machines.upgrade.graph.RoomUpgradeConnection;

public enum GraphEdgeType implements IGraphEdgeType<IGraphEdge> {
    TUNNEL_TYPE(TunnelTypeEdge.CODEC),
    MACHINE_LINK(MachineRoomEdge.CODEC),
    TUNNEL_MACHINE(TunnelMachineEdge.CODEC),
    ROOM_UPGRADE(RoomUpgradeConnection.CODEC);

    private final Codec<IGraphEdge> codec;

    @SuppressWarnings("unchecked")
    GraphEdgeType(Codec<? extends IGraphEdge> codec) {
        this.codec = (Codec<IGraphEdge>) codec;
    }

    @Override
    public Codec<IGraphEdge> codec() {
        return codec;
    }
}
