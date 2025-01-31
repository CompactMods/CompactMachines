package dev.compactmods.machines.room.upgrade;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.room.upgrade.events.lifecycle.UpgradeAppliedEventListener;
import dev.compactmods.machines.api.room.upgrade.events.lifecycle.UpgradeRemovedEventListener;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RoomUpgradeMenuEvents {
    public static void onUpgradeApplied(String roomCode, @NotNull ItemStack itemStack, UUID newInstanceID) {
        final var roomInstance = CompactMachines.room(roomCode).orElseThrow();
        final var accessor = CompactMachines.upgradeAccessor();
        final var upgradeInstance = accessor.getOrCreateInstance(roomInstance, newInstanceID);

        final var upgradeList = itemStack.get(CMDataComponents.UPGRADE_LIST_COMPONENT);
        if (upgradeList != null)
            upgradeList.upgrades()
                    .stream()
                    .flatMap(RoomUpgrade::gatherEvents)
                    .filter(UpgradeAppliedEventListener.class::isInstance)
                    .map(UpgradeAppliedEventListener.class::cast)
                    .forEach(listener -> {
                        listener.handle(upgradeInstance);
                    });
    }

    public static void onUpgradeRemoved(String roomCode, @NotNull ItemStack itemStack, UUID instanceID) {
        final var roomInstance = CompactMachines.room(roomCode).orElseThrow();
        final var accessor = CompactMachines.upgradeAccessor();
        final var upgradeInstance = accessor.getOrCreateInstance(roomInstance, instanceID);

        final var upgradeList = itemStack.get(CMDataComponents.UPGRADE_LIST_COMPONENT);
        if (upgradeList != null)
            upgradeList.upgrades()
                    .stream()
                    .flatMap(RoomUpgrade::gatherEvents)
                    .filter(UpgradeRemovedEventListener.class::isInstance)
                    .map(UpgradeRemovedEventListener.class::cast)
                    .forEach(listener -> {
                        listener.handle(upgradeInstance);
                    });
    }
}

