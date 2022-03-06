package dev.compactmods.machines.graph;

import com.mojang.serialization.Codec;
import dev.compactmods.machines.machine.graph.CompactMachineNode;
import dev.compactmods.machines.tunnel.graph.TunnelMachineEdge;
import dev.compactmods.machines.tunnel.graph.TunnelNode;
import dev.compactmods.machines.tunnel.graph.TunnelTypeEdge;
import dev.compactmods.machines.tunnel.graph.TunnelTypeNode;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Locale;

public class CompactGraphs {

    @Nullable
    @SuppressWarnings("unchecked")
    public static Codec<IGraphNode> getCodecForNode(ResourceLocation nodeType) {
        Codec<? extends IGraphNode> codec = switch (nodeType.getPath().toLowerCase(Locale.ROOT)) {
            case "machine" -> CompactMachineNode.CODEC;
            case "tunnel" -> TunnelNode.CODEC;
            case "tunnel_type" -> TunnelTypeNode.CODEC;
            default -> null;
        };

        if (codec == null)
            return null;

        return (Codec<IGraphNode>) codec;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static Codec<IGraphEdge> getCodecForEdge(ResourceLocation edgeType) {
        Codec<? extends IGraphEdge> codec = switch (edgeType.getPath().toLowerCase(Locale.ROOT)) {
            case "tunnel_machine" -> TunnelMachineEdge.CODEC;
            case "tunnel_type" -> TunnelTypeEdge.CODEC;
            default -> null;
        };

        if (codec == null)
            return null;

        return (Codec<IGraphEdge>) codec;
    }
}
