package dev.compactmods.machines.rooms.history;

import dev.compactmods.machines.teleportation.DimensionalPosition;

public class PlayerRoomHistoryItem implements IRoomHistoryItem {

    private final DimensionalPosition entry;
    private final int machine;

    public PlayerRoomHistoryItem(DimensionalPosition entry, int machine) {
        this.entry = entry;
        this.machine = machine;
    }

    @Override
    public DimensionalPosition getEntryLocation() {
        return entry;
    }

    @Override
    public int getMachine() {
        return machine;
    }
}
