package com.robotgryphon.compactmachines.rooms;

import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;

public interface IRoomHistoryItem {

    DimensionalPosition getEntryLocation();

    int getMachine();
}
