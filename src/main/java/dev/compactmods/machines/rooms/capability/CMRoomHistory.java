package dev.compactmods.machines.rooms.capability;

import java.util.ArrayDeque;
import java.util.Deque;
import dev.compactmods.machines.rooms.history.IRoomHistoryItem;

public class CMRoomHistory implements IRoomHistory{

    private final Deque<IRoomHistoryItem> history;

    public CMRoomHistory() {
        history = new ArrayDeque<>(10);
    }

    @Override
    public void clear() {
        history.clear();
    }

    @Override
    public boolean hasHistory() {
        return !history.isEmpty();
    }

    @Override
    public IRoomHistoryItem peek() {
        return history.peekLast();
    }

    @Override
    public IRoomHistoryItem pop() {
        return history.removeLast();
    }

    @Override
    public void addHistory(IRoomHistoryItem item) {
        history.add(item);
    }
}
