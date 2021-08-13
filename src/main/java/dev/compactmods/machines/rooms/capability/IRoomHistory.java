package dev.compactmods.machines.rooms.capability;

import dev.compactmods.machines.rooms.IRoomHistoryItem;

public interface IRoomHistory {

    boolean hasHistory();
    IRoomHistoryItem peek();
    IRoomHistoryItem pop();

    void addHistory(IRoomHistoryItem item);

}
