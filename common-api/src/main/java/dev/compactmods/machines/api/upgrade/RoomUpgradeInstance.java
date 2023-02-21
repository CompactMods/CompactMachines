package dev.compactmods.machines.api.upgrade;

public record RoomUpgradeInstance<T extends RoomUpgrade>(T upgrade, String room) {}