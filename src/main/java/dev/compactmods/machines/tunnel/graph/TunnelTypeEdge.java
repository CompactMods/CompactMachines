package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.CMGraphRegistration;
import dev.compactmods.machines.graph.GraphEdgeType;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphEdgeType;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public final class TunnelTypeEdge implements IGraphEdge {
    private static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "tunnel_type");

    public static final Codec<TunnelTypeEdge> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (t) -> new TunnelTypeEdge()));

    public TunnelTypeEdge() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj != null && obj.getClass() == this.getClass();
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public String toString() {
        return "TunnelTypeEdge[]";
    }

    @Override
    public @NotNull IGraphEdgeType getEdgeType() {
        return GraphEdgeType.TUNNEL_TYPE;
    }
}
