package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.IGraphNode;
import net.minecraft.resources.ResourceLocation;

public record TunnelTypeNode(ResourceLocation id) implements IGraphNode {
    private static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "tunnel_type");

    public static final Codec<TunnelTypeNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("tunnel_type").forGetter(TunnelTypeNode::id),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (tunn, type) -> new TunnelTypeNode(tunn)));

    @Override
    public Codec<TunnelTypeNode> codec() {
        return CODEC;
    }
}
