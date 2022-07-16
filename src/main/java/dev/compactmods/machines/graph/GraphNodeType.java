package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.room.graph.CompactMachineRoomNode;
import dev.compactmods.machines.tunnel.graph.TunnelNode;
import dev.compactmods.machines.tunnel.graph.TunnelTypeNode;
import dev.compactmods.machines.upgrade.graph.RoomUpgradeGraphNode;
import net.minecraft.resources.ResourceLocation;

public enum GraphNodeType implements IGraphNodeType {
    MACHINE(CompactMachineNode.CODEC),
    TUNNEL(TunnelNode.CODEC),
    ROOM(CompactMachineRoomNode.CODEC),
    TUNNEL_TYPE(TunnelTypeNode.CODEC),
    DIMENSION(DimensionGraphNode.CODEC), ROOM_UPGRADE(RoomUpgradeGraphNode.CODEC);

    private final Codec<IGraphNode> codec;
    private ResourceLocation regName;

    @SuppressWarnings("unchecked")
    <T extends IGraphNode> GraphNodeType(Codec<T> codec) {
        this.codec = (Codec<IGraphNode>) codec;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Codec<IGraphNode> codec() {
        return codec;
    }
}
