package dev.compactmods.machines.api.room.upgrade;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface IRoomUpgradeAccessor {

    Stream<RoomUpgradeInstance> all();

    Optional<RoomUpgradeInstance> getExistingInstance(UUID id);

    RoomUpgradeInstance getOrCreateInstance(UUID id);

    void remove(UUID uuid);

    void clearCache();
}
