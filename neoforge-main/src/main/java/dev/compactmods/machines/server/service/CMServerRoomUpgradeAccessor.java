package dev.compactmods.machines.server.service;

import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.upgrade.IRoomUpgradeAccessor;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class CMServerRoomUpgradeAccessor implements IRoomUpgradeAccessor {

    private final Map<UUID, RoomUpgradeInstance> INSTANCES;
    private final RoomInstance roomInstance;

    public CMServerRoomUpgradeAccessor(RoomInstance roomInstance) {
        INSTANCES = new Object2ObjectOpenHashMap<>();
        this.roomInstance = roomInstance;

        // Load instances off the room upgrade inventory
        roomInstance.getData(CMDataAttachments.UPGRADE_ITEMS)
                .items()
                .filter(is -> is.has(CMDataComponents.UPGRADE_INSTANCE_ID))
                .forEach(is -> {
                    final var id = is.get(CMDataComponents.UPGRADE_INSTANCE_ID);
                    final var instance = getOrCreateInstance(id);
                    INSTANCES.put(id, instance);
                });
    }

    @Override
    public Stream<RoomUpgradeInstance> all() {
        return INSTANCES.values().stream();
    }

    @Override
    public Optional<RoomUpgradeInstance> getExistingInstance(UUID id) {
        return INSTANCES.containsKey(id) ? Optional.of(INSTANCES.get(id)) : Optional.empty();
    }

    @Override
    public RoomUpgradeInstance getOrCreateInstance(UUID id) {
        return getExistingInstance(id).orElseGet(() -> createInstance(id));
    }

    @Override
    public void remove(UUID uuid) {
        INSTANCES.remove(uuid);
    }

    @Override
    public void clearCache() {
        INSTANCES.clear();
    }

    private RoomUpgradeInstance createInstance(UUID id) {
        final var upgradeItem = roomInstance.getData(CMDataAttachments.UPGRADE_ITEMS)
                .items()
                .filter(stack -> stack.has(CMDataComponents.UPGRADE_INSTANCE_ID))
                .filter(stack -> {
                    final var instanceID = stack.get(CMDataComponents.UPGRADE_INSTANCE_ID);
                    if(instanceID == null) return false;
                    return instanceID.equals(id);
                })
                .findFirst();

        final var instance = new RoomUpgradeInstance(roomInstance, id, upgradeItem.orElse(ItemStack.EMPTY));
        INSTANCES.put(id, instance);

        return instance;
    }

}
