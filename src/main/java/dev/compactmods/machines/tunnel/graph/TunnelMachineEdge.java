package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.CMGraphRegistration;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphEdgeType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.Objects;

/**
 * Bridges connection between a tunnel and a given machine side.
 */
public record TunnelMachineEdge(Direction side) implements IGraphEdge {
    private static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "tunnel_machine");

    public static final Codec<TunnelMachineEdge> CODEC = RecordCodecBuilder.create(i -> i.group(
            Direction.CODEC.fieldOf("side").forGetter(TunnelMachineEdge::side),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (side, t) -> new TunnelMachineEdge(side)));

    @Override
    public String toString() {
        return "TunnelMachineEdge[" +
                "side=" + side + ']';
    }

    @Override
    public IGraphEdgeType getEdgeType() {
        return CMGraphRegistration.TUNNEL_MACHINE_LINK.get();
    }
}
