package dev.compactmods.machines.room.capability;

import dev.compactmods.machines.room.history.IRoomHistoryItem;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IRoomHistory extends INBTSerializable<ListTag> {

    void clear();
    boolean hasHistory();
    IRoomHistoryItem peek();
    IRoomHistoryItem pop();

    void addHistory(IRoomHistoryItem item);
}
