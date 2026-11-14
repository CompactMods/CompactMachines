package dev.compactmods.machines.upgrades.system;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.WorldClock;

/// The `compactmachines:tick` world clock — the monotonic tick source that drives room-system staggering.
///
/// Registered as a datapack `world_clock` entry (`tick.json` = `{}`), tagged into
/// `neoforge:world_clock/ignores/advance_time_rule` so it keeps advancing regardless of the daylight-cycle rule.
/// Pausing this clock (its ticks stop advancing) is the framework's global on/off switch.
public final class CMClocks {

    public static final ResourceKey<WorldClock> TICK =
            ResourceKey.create(Registries.WORLD_CLOCK, CompactMachinesCore.identifier("tick"));

    private CMClocks() {
    }

    /// Current total ticks of the `compactmachines:tick` clock for the given level.
    public static long totalTicks(ServerLevel level) {
        final var registry = level.registryAccess().lookupOrThrow(Registries.WORLD_CLOCK);
        final Holder<WorldClock> holder = registry.wrapAsHolder(registry.getValueOrThrow(TICK));
        return level.clockManager().getInstance(holder).totalTicks();
    }
}
