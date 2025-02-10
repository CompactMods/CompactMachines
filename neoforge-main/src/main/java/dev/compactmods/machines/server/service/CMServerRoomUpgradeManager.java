package dev.compactmods.machines.server.service;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.IRoomUpgradeAccessor;
import dev.compactmods.machines.api.room.upgrade.IRoomUpgradeManager;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class CMServerRoomUpgradeManager implements IRoomUpgradeManager {

    private final Map<UUID, String> INSTANCE_LOCATIONS;
    private final Map<String, CMServerRoomUpgradeAccessor> ACCESSORS;

    public CMServerRoomUpgradeManager() {
        ACCESSORS = new Object2ObjectOpenHashMap<>();
        INSTANCE_LOCATIONS = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public void removeInstance(UUID uuid) {
        final var existing = INSTANCE_LOCATIONS.remove(uuid);
        if(existing != null) {
            final var room = ACCESSORS.get(existing);
            room.remove(uuid);
        }
    }

    @Override
    public Optional<RoomUpgradeInstance> get(UUID uuid) {
        if(!INSTANCE_LOCATIONS.containsKey(uuid))
            return Optional.empty();

        String inRoom = INSTANCE_LOCATIONS.get(uuid);
        return ACCESSORS.get(inRoom).getExistingInstance(uuid);
    }

    @Override
    public Stream<UUID> allUpgradesIDs() {
        return INSTANCE_LOCATIONS.keySet().stream();
    }

    @Override
    public Stream<RoomUpgradeInstance> allUpgrades() {
        return allUpgradesIDs()
                .map(this::get)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }

    @Override
    public IRoomUpgradeAccessor upgradeAccessor(RoomInstance roomInstance) {
        return ACCESSORS.computeIfAbsent(roomInstance.code(), roomCode -> new CMServerRoomUpgradeAccessor(roomInstance));
    }

    public void clearCache() {
        INSTANCE_LOCATIONS.clear();
        ACCESSORS.forEach((code, accessor) -> {
            accessor.clearCache();
        });
    }
}
