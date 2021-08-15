package dev.compactmods.machines.rooms.capability;

import dev.compactmods.machines.rooms.history.IRoomHistoryItem;

public interface IRoomHistory {

    void clear();
    boolean hasHistory();
    IRoomHistoryItem peek();
    IRoomHistoryItem pop();

    void addHistory(IRoomHistoryItem item);

}
