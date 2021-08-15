package dev.compactmods.machines.rooms.history;

import dev.compactmods.machines.teleportation.DimensionalPosition;

public interface IRoomHistoryItem {

    DimensionalPosition getEntryLocation();

    int getMachine();
}
