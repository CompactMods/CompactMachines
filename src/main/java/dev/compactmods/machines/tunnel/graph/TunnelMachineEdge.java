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

public final class TunnelMachineEdge extends ForgeRegistryEntry<IGraphEdgeType> implements IGraphEdge {
    private static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "tunnel_machine");

    public static final Codec<TunnelMachineEdge> CODEC = RecordCodecBuilder.create(i -> i.group(
            Direction.CODEC.fieldOf("side").forGetter(TunnelMachineEdge::side),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (side, t) -> new TunnelMachineEdge(side)));
    private final Direction side;

    public TunnelMachineEdge(Direction side) {
        this.side = side;
    }

    public Direction side() {
        return side;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TunnelMachineEdge) obj;
        return Objects.equals(this.side, that.side);
    }

    @Override
    public int hashCode() {
        return Objects.hash(side);
    }

    @Override
    public String toString() {
        return "TunnelMachineEdge[" +
                "side=" + side + ']';
    }

    @Override
    public IGraphEdgeType getEdgeType() {
        return CMGraphRegistration.MACHINE_LINK.get();
    }
}
