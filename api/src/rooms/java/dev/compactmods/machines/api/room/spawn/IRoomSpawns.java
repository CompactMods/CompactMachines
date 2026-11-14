package dev.compactmods.machines.api.room.spawn;

import java.util.Optional;
import java.util.UUID;

public interface IRoomSpawns {
    RoomSpawn defaultSpawn();

    Optional<RoomSpawn> forPlayer(UUID player);
}
