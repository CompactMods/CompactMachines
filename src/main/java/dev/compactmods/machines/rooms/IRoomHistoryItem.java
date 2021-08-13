package dev.compactmods.machines.rooms;

import dev.compactmods.machines.teleportation.DimensionalPosition;

public interface IRoomHistoryItem {

    DimensionalPosition getEntryLocation();

    int getMachine();
}
