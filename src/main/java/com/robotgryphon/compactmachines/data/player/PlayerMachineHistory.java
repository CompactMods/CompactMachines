package com.robotgryphon.compactmachines.data.player;

import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;

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
