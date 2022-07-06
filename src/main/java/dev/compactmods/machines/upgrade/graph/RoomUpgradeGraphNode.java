package dev.compactmods.machines.upgrade.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.graph.CMGraphRegistration;
import dev.compactmods.machines.graph.IGraphNode;
import dev.compactmods.machines.graph.IGraphNodeType;
import net.minecraft.resources.ResourceLocation;

public record RoomUpgradeGraphNode(ResourceLocation key) implements IGraphNode {

    public static final Codec<RoomUpgradeGraphNode> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceLocation.CODEC.fieldOf("upgrade").forGetter(RoomUpgradeGraphNode::key)
    ).apply(i, RoomUpgradeGraphNode::new));

    @Override
    public IGraphNodeType getType() {
        return CMGraphRegistration.ROOM_UPGRADE_NODE.get();
    }
}
