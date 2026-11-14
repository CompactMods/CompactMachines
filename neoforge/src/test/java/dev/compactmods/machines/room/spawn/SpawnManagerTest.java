package dev.compactmods.machines.room.spawn;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SpawnManagerTest {

    private static final UUID PLAYER = UUID.fromString("17690620-725b-354c-8164-51dde1e2df21");

    /** Room 102V-K3G3-TN39 as stored on disk before any player spawn was set — the loaded-from-file path. */
    private static SpawnManager loadedManager() {
        final var json = JsonParser.parseString("""
                {
                  "roomCode": "102V-K3G3-TN39",
                  "player_spawns": {},
                  "default_spawn": {"position": [1032.5, 2.0, 1032.5], "rotation": [0.0, 0.0]},
                  "room_bounds": {"center": [1032.5, 6.5, 1032.5], "dimensions": [23.0, 13.0, 23.0]}
                }""");
        return SpawnManager.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    @Test
    void everySpawnChangeIsReportedForSaving() {
        final var manager = loadedManager();
        final AtomicInteger saves = new AtomicInteger();
        manager.onChanged(saves::incrementAndGet);

        manager.setPlayerSpawn(PLAYER, new Vec3(1025.5, 2, 1036.5), new Vec2(10f, 90f));
        assertEquals(1, saves.get(), "setting a player spawn must persist");

        manager.setDefaultSpawn(new Vec3(1030, 2, 1030), Vec2.ZERO);
        assertEquals(2, saves.get(), "setting the default spawn must persist");

        manager.resetPlayerSpawn(PLAYER);
        assertEquals(3, saves.get(), "resetting a player spawn must persist");

        manager.resetPlayerSpawn(PLAYER);
        assertEquals(3, saves.get(), "resetting a spawn that isn't set changes nothing");
    }

    @Test
    void spawnsOutsideTheRoomAreIgnoredAndNotSaved() {
        final var manager = loadedManager();
        final AtomicInteger saves = new AtomicInteger();
        manager.onChanged(saves::incrementAndGet);

        manager.setPlayerSpawn(PLAYER, new Vec3(0, 64, 0), Vec2.ZERO);
        assertEquals(0, saves.get());
        assertTrue(manager.spawns().forPlayer(PLAYER).isEmpty());
    }

    @Test
    void playerSpawnSurvivesSaveAndLoad() {
        final var manager = loadedManager();
        manager.setPlayerSpawn(PLAYER, new Vec3(1025.5, 2, 1036.5), new Vec2(10f, 90f));

        final var saved = SpawnManager.CODEC.encodeStart(JsonOps.INSTANCE, manager).getOrThrow();
        final var reloaded = SpawnManager.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow();

        final var spawn = reloaded.spawns().forPlayer(PLAYER).orElseThrow();
        assertEquals(new Vec3(1025.5, 2, 1036.5), spawn.position());
        assertEquals(10f, spawn.rotation().x);
        assertEquals(90f, spawn.rotation().y);
    }
}
