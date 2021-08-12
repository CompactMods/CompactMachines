package com.robotgryphon.compactmachines.rooms.capability;

import java.util.Deque;
import com.robotgryphon.compactmachines.rooms.IRoomHistoryItem;

public interface IRoomHistory {

    boolean hasHistory();
    IRoomHistoryItem peek();
    IRoomHistoryItem pop();

    void addHistory(IRoomHistoryItem item);

}
