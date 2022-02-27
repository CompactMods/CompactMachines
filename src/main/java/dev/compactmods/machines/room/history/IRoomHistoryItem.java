package dev.compactmods.machines.room.history;

import dev.compactmods.machines.core.DimensionalPosition;

public interface IRoomHistoryItem {

    DimensionalPosition getEntryLocation();

    int getMachine();
}
