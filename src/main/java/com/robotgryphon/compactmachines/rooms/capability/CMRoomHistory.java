package com.robotgryphon.compactmachines.rooms.capability;

import java.util.ArrayDeque;
import java.util.Deque;
import com.robotgryphon.compactmachines.rooms.IRoomHistoryItem;

public class CMRoomHistory implements IRoomHistory{

    private final Deque<IRoomHistoryItem> history;

    public CMRoomHistory() {
        history = new ArrayDeque<>(10);
    }

    @Override
    public boolean hasHistory() {
        return !history.isEmpty();
    }

    @Override
    public IRoomHistoryItem peek() {
        return history.peek();
    }

    @Override
    public IRoomHistoryItem pop() {
        return history.pop();
    }

    @Override
    public void addHistory(IRoomHistoryItem item) {
        history.add(item);
    }
}
