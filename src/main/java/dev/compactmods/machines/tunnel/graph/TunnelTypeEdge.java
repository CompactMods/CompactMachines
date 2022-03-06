package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.IGraphEdge;
import net.minecraft.resources.ResourceLocation;

public record TunnelTypeEdge() implements IGraphEdge {
    private static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "tunnel_type");

    public static final Codec<TunnelTypeEdge> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (t) -> new TunnelTypeEdge()));

    @Override
    public Codec<TunnelTypeEdge> codec() {
        return CODEC;
    }
}
