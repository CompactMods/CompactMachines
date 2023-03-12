package dev.compactmods.machines.forge.upgrade.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.upgrade.RoomUpgrade;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphEdgeType;
import dev.compactmods.machines.graph.SimpleGraphEdgeType;
import dev.compactmods.machines.forge.upgrade.MachineRoomUpgrades;
import org.jetbrains.annotations.NotNull;

public class RoomUpgradeConnection<T extends RoomUpgrade> implements IGraphEdge<RoomUpgradeConnection<?>> {
    public static final Codec<RoomUpgradeConnection<?>> CODEC = RecordCodecBuilder.create(i -> i.group(
            MachineRoomUpgrades.REGISTRY.get().getCodec().fieldOf("data").forGetter(RoomUpgradeConnection::instance)
    ).apply(i, RoomUpgradeConnection::new));

    public static final IGraphEdgeType<RoomUpgradeConnection<?>> EDGE_TYPE = SimpleGraphEdgeType.instance(CODEC);

    private final T upgradeData;

    public RoomUpgradeConnection(final T instance) {
        this.upgradeData = instance;
    }

    public T instance() {
        return upgradeData;
    }

    @Override
    public @NotNull IGraphEdgeType<RoomUpgradeConnection<?>> getEdgeType() {
        return EDGE_TYPE;
    }
}
