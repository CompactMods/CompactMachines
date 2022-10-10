package dev.compactmods.machines.api.room.upgrade;

public record RoomUpgradeInstance<T extends RoomUpgrade>(T upgrade, String room) {}