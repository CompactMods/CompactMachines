package dev.compactmods.machines.upgrades.api.system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.api.RoomUpgradeCodecs;
import dev.compactmods.machines.upgrades.api.RoomUpgradeComponent;
import dev.compactmods.machines.upgrades.api.event.lifecycle.TickingRoomUpgradeComponent;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.stream.Stream;

/// A datapack-defined room upgrade: tick-scheduling metadata plus a bundle of [RoomUpgradeComponent] instances.
///
/// This is the single upgrade definition — there is no separate tick-system registry. A room enables upgrades by key
/// (see `RoomSystems.ENABLED_UPGRADES`); the dispatcher ticks each enabled upgrade's [TickingRoomUpgradeComponent]s,
/// staggered/scaled by the metadata here. Components are identified purely by type id — they carry no per-instance
/// configuration in this bundle; configuration is planned to arrive through a separate system.
///
/// The window knobs form a **scaling stagger window**: the dispatcher caps how many rooms with this upgrade tick per
/// server tick, so the window grows with the number of rooms that have it enabled and per-tick load stays bounded.
/// Behavior throughput otherwise scales with the `compactmachines:tick` clock rate.
public record CompiledRoomUpgrade(
        int maxRoomsPerTick,
        int minTickWindow,
        int maxTickWindow,
        List<RoomUpgradeComponent> components
) {

    /// Datapack registry: `data/<namespace>/room_upgrades/*.json`.
    public static final ResourceKey<Registry<CompiledRoomUpgrade>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(CompactMachinesCore.identifier("room_upgrades"));

    public static final Codec<CompiledRoomUpgrade> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.optionalFieldOf("max_rooms_per_tick", 8).forGetter(CompiledRoomUpgrade::maxRoomsPerTick),
            Codec.INT.optionalFieldOf("min_tick_window", 1).forGetter(CompiledRoomUpgrade::minTickWindow),
            Codec.INT.optionalFieldOf("max_tick_window", 200).forGetter(CompiledRoomUpgrade::maxTickWindow),
            // Each list entry is { "type": "<component type>" }, via the component dispatch codec.
            RoomUpgradeCodecs.DISPATCH_CODEC.listOf().fieldOf("components").forGetter(CompiledRoomUpgrade::components)
    ).apply(i, CompiledRoomUpgrade::new));

    /// This upgrade's components that actually tick.
    public Stream<TickingRoomUpgradeComponent> tickingComponents() {
        return components.stream()
                .filter(TickingRoomUpgradeComponent.class::isInstance)
                .map(TickingRoomUpgradeComponent.class::cast);
    }

    public boolean hasTickingComponents() {
        return components.stream().anyMatch(TickingRoomUpgradeComponent.class::isInstance);
    }

    // --- scaling stagger window ---

    /// Window width for a given active-room count: {@code ceil(count / maxRoomsPerTick)}, clamped to [min, max].
    public int tickWindow(int activeCount) {
        int scaled = Mth.ceil(activeCount / (float) Math.max(1, maxRoomsPerTick));
        return Mth.clamp(scaled, minTickWindow, maxTickWindow);
    }

    /// How many of this room's stagger slots fall in the inclusive clock-tick range `[from, to]`.
    ///
    /// Lets the dispatcher scale work to the clock's *rate*: when the clock advances by N ticks in one server tick,
    /// a `window == 1` room ticks N times, so raising the clock rate speeds upgrades.
    public long tickCount(long from, long to, RoomInstance room, int window) {
        if (to < from) return 0L;
        if (window <= 1) return to - from + 1L;
        final long slot = Math.floorMod((long) room.code().hashCode(), (long) window);
        return Math.floorDiv(to - slot, (long) window) - Math.floorDiv(from - 1L - slot, (long) window);
    }
}
