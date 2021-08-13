package dev.compactmods.machines.data.player;

import dev.compactmods.machines.teleportation.DimensionalPosition;

import java.util.Deque;

public class PlayerMachineHistory {

    protected Deque<DimensionalPosition> history;

    public DimensionalPosition getLastEntryPoint() {
        return history.getLast();
    }

    public DimensionalPosition getOrigin() {
        return history.getFirst();
    }
}
