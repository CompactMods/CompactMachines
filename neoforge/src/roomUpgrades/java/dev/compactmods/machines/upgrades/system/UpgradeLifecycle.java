package dev.compactmods.machines.upgrades.system;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponent;
import dev.compactmods.machines.upgrades.api.event.RoomUpgradeComponentEvent;
import dev.compactmods.machines.upgrades.api.event.lifecycle.UpgradeAppliedEventListener;
import dev.compactmods.machines.upgrades.api.event.lifecycle.UpgradeRemovedEventListener;
import dev.compactmods.machines.upgrades.api.system.CompiledRoomUpgrade;
import dev.compactmods.machines.upgrades.canvas.RoomCanvases;

/// Dispatches non-ticking upgrade lifecycle events. Enabling/disabling a [CompiledRoomUpgrade] bundle on a room
/// applies/removes each of its components, firing the matching listener from `RoomUpgradeComponent.gatherEvents()`.
public final class UpgradeLifecycle {

    private UpgradeLifecycle() {
    }

    /// Fired when {@code upgrade} is newly enabled on {@code room}.
    public static void onEnabled(RoomInstance room, CompiledRoomUpgrade upgrade) {
        dispatch(room, upgrade, UpgradeAppliedEventListener.class);
        rebuildCanvas(room);
    }

    /// Fired when {@code upgrade} is newly disabled on {@code room}.
    public static void onDisabled(RoomInstance room, CompiledRoomUpgrade upgrade) {
        dispatch(room, upgrade, UpgradeRemovedEventListener.class);
        rebuildCanvas(room);
    }

    /** The room's canvas offers its enabled upgrades' nodes, so it changes with them. */
    private static void rebuildCanvas(RoomInstance room) {
        final var server = room.level().getServer();
        if (server != null)
            RoomCanvases.invalidate(server, room);
    }

    private static void dispatch(RoomInstance room, CompiledRoomUpgrade upgrade, Class<? extends RoomUpgradeComponentEvent> type) {
        upgrade.components().stream()
                .flatMap(RoomUpgradeComponent::gatherEvents)
                .filter(type::isInstance)
                .forEach(event -> event.handle(room));
    }
}
