package dev.compactmods.machines.rooms.capability;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import com.mojang.serialization.DataResult;
import dev.compactmods.machines.data.codec.NbtListCollector;
import dev.compactmods.machines.rooms.history.IRoomHistoryItem;
import dev.compactmods.machines.rooms.history.PlayerRoomHistoryItem;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;

public class CMRoomHistory implements IRoomHistory {

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

    @Override
    public ListNBT serializeNBT() {
        return history.stream()
                .map(hi -> PlayerRoomHistoryItem.CODEC.encodeStart(NBTDynamicOps.INSTANCE, hi))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(x -> x)
                .collect(NbtListCollector.toNbtList());
    }

    @Override
    public void deserializeNBT(ListNBT nbt) {
        nbt.stream()
                .map(it -> PlayerRoomHistoryItem.CODEC.parse(NBTDynamicOps.INSTANCE, it))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(history::addLast);
    }
}
