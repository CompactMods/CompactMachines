package dev.compactmods.machines.api.room.upgrade;

import dev.compactmods.machines.api.room.RoomInstance;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface IRoomUpgradeManager {

    Optional<RoomUpgradeInstance> get(UUID uuid);

    IRoomUpgradeAccessor upgradeAccessor(RoomInstance roomInstance);

    void clearCache();

    void removeInstance(UUID uuid);

    Stream<UUID> allUpgradesIDs();

    Stream<RoomUpgradeInstance> allUpgrades();
}
