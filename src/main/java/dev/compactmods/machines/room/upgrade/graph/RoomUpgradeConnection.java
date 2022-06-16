package dev.compactmods.machines.room.upgrade.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.graph.GraphEdgeType;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.graph.IGraphEdgeType;
import dev.compactmods.machines.room.upgrade.MachineRoomUpgrades;
import org.jetbrains.annotations.NotNull;

public class RoomUpgradeConnection<T extends RoomUpgrade> implements IGraphEdge {
    public static final Codec<RoomUpgradeConnection<?>> CODEC = RecordCodecBuilder.create(i -> i.group(
            MachineRoomUpgrades.REGISTRY.get().getCodec().fieldOf("data").forGetter(RoomUpgradeConnection::instance)
    ).apply(i, RoomUpgradeConnection::new));

    private final T upgradeData;

    public RoomUpgradeConnection(final T instance) {
        this.upgradeData = instance;
    }

    public T instance() {
        return upgradeData;
    }

    @Override
    public @NotNull IGraphEdgeType getEdgeType() {
        return GraphEdgeType.ROOM_UPGRADE;
    }
}
