package dev.compactmods.machines.api.room.history;

import dev.compactmods.machines.api.data.Saveable;

public interface IPlayerHistoryApi extends Saveable {

   IPlayerEntryPointHistoryManager entryPoints();
   
}
