package dev.compactmods.machines.forge.upgrade.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.upgrade.RoomUpgrade;
import dev.compactmods.machines.graph.IGraphEdge;
import dev.compactmods.machines.forge.upgrade.MachineRoomUpgrades;
import org.jetbrains.annotations.NotNull;

public record RoomUpgradeConnection<T extends RoomUpgrade>(T instance) implements IGraphEdge<RoomUpgradeConnection<?>> {
    public static final Codec<RoomUpgradeConnection<?>> CODEC = RecordCodecBuilder.create(i -> i.group(
            MachineRoomUpgrades.REGISTRY.get().getCodec().fieldOf("data").forGetter(RoomUpgradeConnection::instance)
    ).apply(i, RoomUpgradeConnection::new));

    @Override
    public @NotNull Codec<RoomUpgradeConnection<?>> codec() {
        return CODEC;
    }
}
