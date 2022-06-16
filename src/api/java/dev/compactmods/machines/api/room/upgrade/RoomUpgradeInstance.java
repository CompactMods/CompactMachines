package dev.compactmods.machines.api.room.upgrade;

import net.minecraft.world.level.ChunkPos;

public record RoomUpgradeInstance<T extends RoomUpgrade>(T upgrade, ChunkPos room) {
}
