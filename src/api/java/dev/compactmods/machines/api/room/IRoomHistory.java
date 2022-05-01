package dev.compactmods.machines.api.room;

import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import net.minecraft.nbt.ListTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IRoomHistory<T extends IRoomHistoryItem> extends INBTSerializable<ListTag> {

    void clear();
    boolean hasHistory();
    T peek();
    T pop();

    void addHistory(T item);
}
