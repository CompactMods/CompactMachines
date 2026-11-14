package dev.compactmods.machines.room.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CMRoomDataMigrationTest {

    @TempDir
    Path world;

    private Path legacy() { return world.resolve("compactmachines"); }
    private Path current() { return world.resolve("data").resolve("compactmachines"); }

    private static void write(Path file, String content) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, content);
    }

    @Test
    void movesLegacyFoldersIntoEmptyNewLocation() throws IOException {
        write(legacy().resolve("registry/AAAA-BBBB-CCCC.dat"), "room");
        write(legacy().resolve("player_spawns/AAAA-BBBB-CCCC.dat"), "spawn");
        // A newer build created these empty before the migration existed
        Files.createDirectories(current().resolve("registry"));
        Files.createDirectories(current().resolve("player_spawns"));

        CMRoomDataMigration.migrateLegacyLayout(legacy(), current());

        assertEquals("room", Files.readString(current().resolve("registry/AAAA-BBBB-CCCC.dat")));
        assertEquals("spawn", Files.readString(current().resolve("player_spawns/AAAA-BBBB-CCCC.dat")));
        assertFalse(Files.exists(legacy().resolve("registry")));
        assertFalse(Files.exists(legacy().resolve("player_spawns")));
    }

    @Test
    void createsNewRootWhenMissing() throws IOException {
        write(legacy().resolve("registry/AAAA-BBBB-CCCC.dat"), "room");

        CMRoomDataMigration.migrateLegacyLayout(legacy(), current());

        assertTrue(Files.exists(current().resolve("registry/AAAA-BBBB-CCCC.dat")));
    }

    @Test
    void leavesTeleportHistoryAtLegacyRoot() throws IOException {
        write(legacy().resolve("teleport_history/some-player"), "history");

        CMRoomDataMigration.migrateLegacyLayout(legacy(), current());

        assertTrue(Files.exists(legacy().resolve("teleport_history/some-player")));
        assertFalse(Files.exists(current().resolve("teleport_history")));
    }

    @Test
    void neverMergesTwoPopulatedFolders() throws IOException {
        write(legacy().resolve("registry/OLD0-OLD0-OLD0.dat"), "old");
        write(current().resolve("registry/NEW0-NEW0-NEW0.dat"), "new");

        CMRoomDataMigration.migrateLegacyLayout(legacy(), current());

        assertTrue(Files.exists(legacy().resolve("registry/OLD0-OLD0-OLD0.dat")));
        assertTrue(Files.exists(current().resolve("registry/NEW0-NEW0-NEW0.dat")));
        assertFalse(Files.exists(current().resolve("registry/OLD0-OLD0-OLD0.dat")));
    }

    @Test
    void noLegacyRootIsANoOp() {
        CMRoomDataMigration.migrateLegacyLayout(legacy(), current());

        assertFalse(Files.exists(current()));
    }
}
