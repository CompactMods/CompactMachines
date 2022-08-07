package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.graph.Graph;
import dev.compactmods.machines.graph.GraphNodeBase;
import dev.compactmods.machines.graph.IGraphNodeType;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public final class TunnelTypeNode extends GraphNodeBase implements IGraphNodeType {
    private static final ResourceLocation TYPE = new ResourceLocation(Constants.MOD_ID, "tunnel_type");

    public static final Codec<TunnelTypeNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("tunnel_type").forGetter(TunnelTypeNode::id),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (tunn, type) -> new TunnelTypeNode(tunn)));
    private final ResourceLocation id;

    public TunnelTypeNode() {
        this.id = null;
    }

    public TunnelTypeNode(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public Codec<TunnelTypeNode> codec() {
        return CODEC;
    }

    public ResourceLocation id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TunnelTypeNode) obj;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TunnelTypeNode[" +
                "id=" + id + ']';
    }

    @Override
    public IGraphNodeType getType() {
        return Graph.TUNNEL_TYPE_NODE.get();
    }
}
