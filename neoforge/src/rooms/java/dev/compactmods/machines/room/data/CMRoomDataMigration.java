package dev.compactmods.machines.room.data;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Moves room data written by older builds from {@code <world>/compactmachines/} into
 * {@link CMRoomDataLocations#DATA_ROOT} ({@code <world>/data/compactmachines/}).
 *
 * <p>Must run before the room registry or spawn managers first touch their files, so it is
 * hooked on server about-to-start. Each folder is only moved when the new location is missing
 * or empty — two populated folders are never merged; that case is logged and left alone.
 */
public final class CMRoomDataMigration {

    /**
     * Folders that moved under {@link CMRoomDataLocations#DATA_ROOT}. {@code teleport_history}
     * is deliberately absent — it is still read from the old root.
     */
    private static final List<String> MOVED_FOLDERS = List.of("registry", "player_spawns", "room_data", "room_registrations");

    private CMRoomDataMigration() {}

    public static void migrateLegacyLayout(MinecraftServer server) {
        final Path legacyRoot = server.getServerDirectory()
                .resolve(server.getWorldPath(LevelResource.ROOT))
                .resolve(CompactMachinesCore.MOD_ID);

        migrateLegacyLayout(legacyRoot, CMRoomDataLocations.DATA_ROOT.apply(server));
    }

    static void migrateLegacyLayout(Path legacyRoot, Path newRoot) {
        if (!Files.isDirectory(legacyRoot))
            return;

        final var log = CompactMachinesCore.modLog();

        for (String folder : MOVED_FOLDERS) {
            final Path from = legacyRoot.resolve(folder);
            final Path to = newRoot.resolve(folder);

            try {
                if (!Files.isDirectory(from) || isEmptyDirectory(from))
                    continue;

                if (Files.isDirectory(to)) {
                    if (!isEmptyDirectory(to)) {
                        log.warn("Legacy room data found at {} but {} already has data; leaving both in place.", from, to);
                        continue;
                    }

                    // Created empty by a newer build before the migration existed
                    Files.delete(to);
                }

                Files.createDirectories(newRoot);
                Files.move(from, to);
                log.info("Migrated legacy room data: {} -> {}", from, to);
            } catch (IOException e) {
                log.error("Failed to migrate legacy room data from {} to {}", from, to, e);
            }
        }
    }

    private static boolean isEmptyDirectory(Path dir) throws IOException {
        try (var entries = Files.list(dir)) {
            return entries.findAny().isEmpty();
        }
    }
}
