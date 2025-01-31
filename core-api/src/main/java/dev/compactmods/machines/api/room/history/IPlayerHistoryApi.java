package dev.compactmods.machines.api.room.history;

public interface IPlayerHistoryApi {

   IPlayerEntryPointHistoryManager entryPoints();

    void save();
}
