package dev.compactmods.machines.api.room.spawn;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.core.data.Saveable;

public interface IRoomSpawnManagers extends Saveable {

    IRoomSpawnManager get(RoomInstance room);
}
