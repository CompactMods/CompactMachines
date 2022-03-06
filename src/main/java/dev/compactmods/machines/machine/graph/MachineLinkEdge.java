package dev.compactmods.machines.machine.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.tunnel.graph.TunnelMachineEdge;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public record MachineLinkEdge() implements IGraphEdge {
    private static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "tunnel_machine");

    public static final Codec<TunnelMachineEdge> CODEC = RecordCodecBuilder.create(i -> i.group(
            Direction.CODEC.fieldOf("side").forGetter(TunnelMachineEdge::side),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (side, t) -> new TunnelMachineEdge(side)));

    @Override
    public Codec<TunnelMachineEdge> codec() {
        return CODEC;
    }
}
