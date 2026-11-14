package dev.compactmods.machines.upgrades.example;

import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.registry.RoomRegistry;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.upgrades.api.system.CompiledRoomUpgrade;
import dev.compactmods.machines.upgrades.system.RoomSystems;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Owns the NeoForge {@link TicketController} that force-loads chunks for the chunk-loader upgrade.
 *
 * <p>Chunks are forced with {@code ticking = true}, i.e. they receive full chunk ticks even with no
 * player nearby — full simulation, as if a player were inside the room. Forced chunks are persisted
 * by NeoForge and reinstated automatically on load; the {@linkplain #validate validation callback}
 * prunes tickets for rooms that no longer have the upgrade.
 */
public final class ChunkLoaderTickets {

    private static final TicketController CONTROLLER = new TicketController(
            CompactMachinesCore.identifier("chunkloader_upgrade"), ChunkLoaderTickets::validate);

    private ChunkLoaderTickets() {}

    /** Registers the controller. Call during {@code RegisterTicketControllersEvent}. */
    public static void register(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    /** Forces (or releases) full-simulation tickets for every inner chunk of {@code room}. */
    public static void setForced(RoomInstance room, boolean add) {
        final ServerLevel level = room.level();
        final UUID owner = ownerFor(room.code());
        room.boundaries().innerChunkPositions().forEach(cp -> {
            final long packed = cp.pack();
            CONTROLLER.forceChunk(level, owner, ChunkPos.getX(packed), ChunkPos.getZ(packed), add, true);
        });
    }

    /** Stable per-room ticket owner derived from the room code (room codes are not UUIDs). */
    static UUID ownerFor(String roomCode) {
        return UUID.nameUUIDFromBytes(("compactmachines:chunkloader:" + roomCode).getBytes(StandardCharsets.UTF_8));
    }

    // Runs when persisted tickets are reinstated on level load. Drops any owner whose room no longer
    // has the chunk-loader upgrade. Conservative: if the room registry isn't populated yet (early
    // load) we compute an empty expected-set and keep everything, rather than risk dropping valid tickets.
    private static void validate(ServerLevel level, TicketHelper ticketHelper) {
        final RoomRegistry rooms = RoomCapabilities.REGISTRY.getCapability(level.getServer());
        if (rooms == null)
            return;

        final Registry<CompiledRoomUpgrade> upgrades = level.registryAccess().lookupOrThrow(CompiledRoomUpgrade.REGISTRY_KEY);

        final Set<UUID> expected = rooms.allRooms()
                .filter(room -> hasChunkLoader(room, upgrades))
                .map(room -> ownerFor(room.code()))
                .collect(Collectors.toSet());

        if (expected.isEmpty())
            return;

        ticketHelper.getEntityTickets().keySet().stream()
                .filter(owner -> !expected.contains(owner))
                .toList()
                .forEach(ticketHelper::removeAllTickets);
    }

    /** {@return true if any upgrade bundle enabled on {@code room} contains a chunk-loader component} */
    static boolean hasChunkLoader(RoomInstance room, Registry<CompiledRoomUpgrade> upgrades) {
        return room.getData(RoomSystems.ENABLED_UPGRADES).stream()
                .map(upgrades::getValue)
                .filter(Objects::nonNull)
                .flatMap(upgrade -> upgrade.components().stream())
                .anyMatch(ChunkLoaderUpgradeComponent.class::isInstance);
    }
}
