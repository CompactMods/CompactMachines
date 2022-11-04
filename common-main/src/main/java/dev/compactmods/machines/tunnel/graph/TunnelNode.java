package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.tunnels.connection.RoomTunnelConnections;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.graph.SimpleGraphNodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Optional;

public record TunnelNode(BlockPos position) implements IGraphNode<TunnelNode> {
    private static final ResourceLocation TYPE = new ResourceLocation(Constants.MOD_ID, "tunnel");

    public static final Codec<TunnelNode> CODEC = RecordCodecBuilder.create((i) -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(TunnelNode::position),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (bpos, type) -> new TunnelNode(bpos)));

    public static final IGraphNodeType<TunnelNode> NODE_TYPE = SimpleGraphNodeType.instance(CODEC);

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TunnelNode) obj;
        return Objects.equals(this.position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position);
    }

    @Override
    public String toString() {
        return "TunnelNode[position=%s]".formatted(position);
    }

    @Override
    public IGraphNodeType<TunnelNode> getType() {
        return NODE_TYPE;
    }

    public Optional<Direction> getTunnelSide(RoomTunnelConnections connections) {
        return connections.getConnectedSide(position);
    }
}
