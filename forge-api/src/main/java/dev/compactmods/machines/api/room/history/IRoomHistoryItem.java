package dev.compactmods.machines.api.room.history;

import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import dev.compactmods.machines.api.location.IDimensionalPosition;

public interface IRoomHistoryItem {

    IDimensionalPosition getEntryLocation();

    IDimensionalBlockPosition getMachine();
}
