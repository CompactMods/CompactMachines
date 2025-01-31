package dev.compactmods.machines.server.services;

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

public class CMServerRoomUpgradeAccessor implements IRoomUpgradeAccessor {

    private final Map<UUID, RoomUpgradeInstance> INSTANCES;

    public CMServerRoomUpgradeAccessor() {
        INSTANCES = new Object2ObjectOpenHashMap<>();
    }

    @Override
    public Optional<RoomUpgradeInstance> getExistingInstance(RoomInstance roomInstance, UUID id) {
        return INSTANCES.containsKey(id) ? Optional.of(INSTANCES.get(id)) : Optional.empty();
    }

    @Override
    public RoomUpgradeInstance getOrCreateInstance(RoomInstance roomInstance, UUID id) {
        return getExistingInstance(roomInstance, id).orElseGet(() -> createInstance(roomInstance, id));
    }

    private RoomUpgradeInstance createInstance(RoomInstance roomInstance, UUID id) {
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

    @Override
    public void removeInstance(UUID uuid) {
        INSTANCES.remove(uuid);
    }

    public void clearCache() {
        INSTANCES.clear();
    }
}
