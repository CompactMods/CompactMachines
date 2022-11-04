package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.graph.SimpleGraphNodeType;
import net.minecraft.resources.ResourceLocation;

public record TunnelTypeNode(ResourceLocation id) implements IGraphNodeType, IGraphNode {
    private static final ResourceLocation TYPE = new ResourceLocation(Constants.MOD_ID, "tunnel_type");

    public static final Codec<TunnelTypeNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("tunnel_type").forGetter(TunnelTypeNode::id),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (tunn, type) -> new TunnelTypeNode(tunn)));

    public static final IGraphNodeType<TunnelTypeNode> NODE_TYPE = SimpleGraphNodeType.instance(CODEC);

    @Override
    public Codec<TunnelTypeNode> codec() {
        return CODEC;
    }

    @Override
    public IGraphNodeType getType() {
        return NODE_TYPE;
    }
}
