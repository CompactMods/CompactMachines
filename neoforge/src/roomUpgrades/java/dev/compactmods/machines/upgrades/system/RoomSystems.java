package dev.compactmods.machines.upgrades.system;

import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.upgrades.api.system.CompiledRoomUpgrade;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NewDatapackRegistryEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/// Wires the room-upgrade tick framework: registers the [CompiledRoomUpgrade] datapack registry, the per-room
/// `ENABLED_UPGRADES` attachment, and the level-tick dispatcher.
public final class RoomSystems {

    /// Persisted room attachment: the set of [CompiledRoomUpgrade] bundles enabled on a room.
    public static final Supplier<AttachmentType<Set<ResourceKey<CompiledRoomUpgrade>>>> ENABLED_UPGRADES =
            Rooms.ATTACHMENT_TYPES.register("enabled_upgrades", () -> AttachmentType
                    .<Set<ResourceKey<CompiledRoomUpgrade>>>builder(() -> new HashSet<>())
                    .serialize(ResourceKey.codec(CompiledRoomUpgrade.REGISTRY_KEY).listOf()
                            .<Set<ResourceKey<CompiledRoomUpgrade>>>xmap(HashSet::new, List::copyOf)
                            .fieldOf("upgrades"))
                    .build());

    private RoomSystems() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(RoomSystems::onNewDataPackRegistry);
        NeoForge.EVENT_BUS.addListener(RoomSystemManager::onLevelTick);
    }

    private static void onNewDataPackRegistry(NewDatapackRegistryEvent event) {
        // Server-only (ticking never runs client-side) — no network codec / no client sync.
        event.worldRegistry(CompiledRoomUpgrade.REGISTRY_KEY, CompiledRoomUpgrade.CODEC);
    }
}
