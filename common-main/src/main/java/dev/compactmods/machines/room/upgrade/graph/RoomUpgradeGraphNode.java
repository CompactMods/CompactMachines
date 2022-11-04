package dev.compactmods.machines.room.upgrade.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import dev.compactmods.machines.graph.SimpleGraphNodeType;
import net.minecraft.resources.ResourceLocation;

public record RoomUpgradeGraphNode(ResourceLocation key) implements IGraphNode {

    public static final Codec<RoomUpgradeGraphNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("upgrade").forGetter(RoomUpgradeGraphNode::key)
    ).apply(i, RoomUpgradeGraphNode::new));

    public static final IGraphNodeType<RoomUpgradeGraphNode> NODE_TYPE = SimpleGraphNodeType.instance(CODEC);

    @Override
    public IGraphNodeType getType() {
        return NODE_TYPE;
    }
}
