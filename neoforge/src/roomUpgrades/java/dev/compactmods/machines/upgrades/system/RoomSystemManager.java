package dev.compactmods.machines.upgrades.system;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.upgrades.api.event.lifecycle.TickingRoomUpgradeComponent;
import dev.compactmods.machines.upgrades.api.system.CompiledRoomUpgrade;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/// The single level-tick dispatcher for room upgrades, scoped to the compact dimension.
///
/// Registered on the game event bus by [RoomSystems#init]. Each firing: gate on the compact dimension, a normally
/// running tick-rate, and an advancing clock (a non-advancing clock == globally disabled). Then, grouped by enabled
/// [CompiledRoomUpgrade], size the scaling window from the number of rooms that have it, and tick the staggered slice.
public final class RoomSystemManager {

    /// Last clock tick we dispatched up to. {@code MIN_VALUE} until the first firing establishes a baseline.
    private static long lastTick = Long.MIN_VALUE;

    /// Upper bound on clock ticks processed in a single server tick, so a very high clock rate — or a one-off clock
    /// jump (manual set, first load) — can't freeze the server. Comfortably above typical fast-clock rates.
    private static final long MAX_CATCHUP = 20_000L;

    private RoomSystemManager() {
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!level.dimension().equals(CompactDimension.LEVEL_KEY)) return;
        if (!level.tickRateManager().runsNormally()) return;

        final long now = CMClocks.totalTicks(level);
        final long previous = lastTick;
        lastTick = now;
        if (previous == Long.MIN_VALUE) return;   // first observation — just establish the baseline
        final long elapsed = now - previous;
        if (elapsed <= 0L) return;                // clock didn't advance (paused) or was reset — global disable

        // Consume the clock delta so throughput scales with the clock's rate; cap the catch-up to stay safe.
        final long steps = Math.min(elapsed, MAX_CATCHUP);
        final long from = now - steps + 1L;

        final RoomRegistry rooms = RoomCapabilities.REGISTRY.getCapability(level.getServer());
        if (rooms == null) return;

        final Registry<CompiledRoomUpgrade> upgrades = level.registryAccess().lookupOrThrow(CompiledRoomUpgrade.REGISTRY_KEY);

        // Group rooms by the upgrade key they have enabled (one room scan).
        final Map<ResourceKey<CompiledRoomUpgrade>, List<RoomInstance>> roomsByUpgrade = new LinkedHashMap<>();
        rooms.allRooms().forEach(room -> {
            for (ResourceKey<CompiledRoomUpgrade> key : room.getData(RoomSystems.ENABLED_UPGRADES))
                roomsByUpgrade.computeIfAbsent(key, k -> new ArrayList<>()).add(room);
        });
        if (roomsByUpgrade.isEmpty()) return;

        roomsByUpgrade.forEach((key, active) -> {
            final CompiledRoomUpgrade upgrade = upgrades.getValue(key);
            if (upgrade == null || !upgrade.hasTickingComponents()) return;

            final List<TickingRoomUpgradeComponent> tickers = upgrade.tickingComponents().toList();
            final int window = upgrade.tickWindow(active.size());

            for (RoomInstance room : active) {
                final long count = upgrade.tickCount(from, now, room, window);
                for (long i = 0; i < count; i++)
                    for (TickingRoomUpgradeComponent ticker : tickers)
                        ticker.tick(room);
            }
        });
    }
}
