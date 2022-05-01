package dev.compactmods.machines.api.room.history;

import dev.compactmods.machines.api.location.IDimensionalPosition;

public interface IRoomHistoryItem {

    IDimensionalPosition getEntryLocation();

    int getMachine();
}
