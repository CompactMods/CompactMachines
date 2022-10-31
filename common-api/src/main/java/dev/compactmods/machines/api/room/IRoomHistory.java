package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.room.history.IRoomHistoryItem;

public interface IRoomHistory<T extends IRoomHistoryItem> {

    void clear();
    boolean hasHistory();
    T peek();
    T pop();

    void addHistory(T item);
}
