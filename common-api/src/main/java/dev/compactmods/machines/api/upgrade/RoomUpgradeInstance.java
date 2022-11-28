package dev.compactmods.machines.api.upgrade;

public record RoomUpgradeInstance<T extends RoomUpgradeAction>(T upgrade, String room) {}