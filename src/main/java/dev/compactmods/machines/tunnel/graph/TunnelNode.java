package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.CMGraphRegistration;
import dev.compactmods.machines.graph.GraphNodeBase;
import dev.compactmods.machines.graph.IGraphNodeType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class TunnelNode extends GraphNodeBase implements IGraphNodeType {
    private static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "tunnel");

    public static final Codec<TunnelNode> CODEC = RecordCodecBuilder.create((i) -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(TunnelNode::position),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (bpos, type) -> new TunnelNode(bpos)));
    private final BlockPos position;

    public TunnelNode() {
        this.position = BlockPos.ZERO;
    }

    public TunnelNode(BlockPos position) {
        this.position = position;
    }

    @Override
    public Codec<TunnelNode> codec() {
        return CODEC;
    }

    public BlockPos position() {
        return position;
    }

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
        return "TunnelNode[" +
                "position=" + position + ']';
    }

    @Override
    public IGraphNodeType getType() {
        return CMGraphRegistration.TUNNEL_NODE.get();
    }
}
