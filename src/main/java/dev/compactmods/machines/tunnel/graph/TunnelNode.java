package dev.compactmods.machines.tunnel.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.graph.IGraphNode;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record TunnelNode(BlockPos position) implements IGraphNode {
    private static final ResourceLocation TYPE = new ResourceLocation(CompactMachines.MOD_ID, "tunnel");

    public static final Codec<TunnelNode> CODEC = RecordCodecBuilder.create((i) -> i.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(TunnelNode::position),
            ResourceLocation.CODEC.fieldOf("type").forGetter(x -> TYPE)
    ).apply(i, (bpos, type) -> new TunnelNode(bpos)));

    @Override
    public Codec<TunnelNode> codec() {
        return CODEC;
    }
}
