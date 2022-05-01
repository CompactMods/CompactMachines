package dev.compactmods.machines.room.capability;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import com.mojang.serialization.DataResult;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.api.codec.NbtListCollector;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.room.history.PlayerRoomHistoryItem;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;

public class CMRoomHistory implements IRoomHistory<PlayerRoomHistoryItem> {

    private final Deque<PlayerRoomHistoryItem> history;

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
    public PlayerRoomHistoryItem peek() {
        return history.peekLast();
    }

    @Override
    public PlayerRoomHistoryItem pop() {
        return history.removeLast();
    }

    @Override
    public void addHistory(PlayerRoomHistoryItem item) {
        history.add(item);
    }

    @Override
    public ListTag serializeNBT() {
        return history.stream()
                .map(hi -> PlayerRoomHistoryItem.CODEC.encodeStart(NbtOps.INSTANCE, hi))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(x -> x)
                .collect(NbtListCollector.toNbtList());
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        nbt.stream()
                .map(it -> PlayerRoomHistoryItem.CODEC.parse(NbtOps.INSTANCE, it))
                .map(DataResult::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(history::addLast);
    }
}
