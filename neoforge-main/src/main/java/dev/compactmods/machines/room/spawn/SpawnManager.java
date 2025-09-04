package dev.compactmods.machines.room.spawn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.data.CMRoomDataLocations;
import dev.compactmods.machines.data.CMDataFile;
import dev.compactmods.machines.api.room.spatial.IRoomBoundaries;
import dev.compactmods.machines.api.room.spawn.IRoomSpawn;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawns;
import dev.compactmods.machines.data.CodecHolder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class SpawnManager implements IRoomSpawnManager, CodecHolder<SpawnManager>, CMDataFile {

    private final Logger LOGS = LogManager.getLogger();

    private static final UnboundedMapCodec<UUID, RoomSpawn> PLAYER_SPAWNS_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, RoomSpawn.CODEC);
    private static final Codec<SpawnManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("roomCode").forGetter(x -> x.roomCode),
            PLAYER_SPAWNS_CODEC.fieldOf("player_spawns").forGetter(x -> x.playerSpawns),
            RoomSpawn.CODEC.fieldOf("default_spawn").forGetter(x -> x.defaultSpawn)
    ).apply(inst, SpawnManager::new));

    private final String roomCode;

    @Nullable
    private RoomInstance roomInstance;

    @Nullable
    private RoomSpawn defaultSpawn;

    private final Map<UUID, RoomSpawn> playerSpawns;

    public SpawnManager(String roomCode) {
        this.roomCode = roomCode;
        this.playerSpawns = new HashMap<>();
    }

    private SpawnManager(String roomCode, Map<UUID, RoomSpawn> playerSpawns, RoomSpawn defaultSpawn) {
        this.roomCode = roomCode;
        this.playerSpawns = new HashMap<>(playerSpawns);
        this.defaultSpawn = defaultSpawn;
    }

    @Override
    public void resetPlayerSpawn(UUID player) {
        playerSpawns.remove(player);
    }

    @Override
    public void setDefaultSpawn(Vec3 position, Vec2 rotation) {
        defaultSpawn = new RoomSpawn(position, rotation);
    }

    @Override
    public IRoomSpawns spawns() {
        if (this.defaultSpawn == null) {
            final var i = getRoomInstance();
            this.defaultSpawn = new RoomSpawn(i.boundaries().defaultSpawn(), Vec2.ZERO);
        }

        final var ps = new HashMap<UUID, RoomSpawn>();
        playerSpawns.forEach(ps::putIfAbsent);
        return new RoomSpawns(defaultSpawn, ps);
    }

    @Override
    public void setPlayerSpawn(UUID player, Vec3 location, Vec2 rotation) {
        final var ins = getRoomInstance();
        if (!ins.boundaries().innerBounds().contains(location))
            return;

        playerSpawns.put(player, new RoomSpawn(location, rotation));
    }

    @Override
    public Path getDataLocation(MinecraftServer server) {
        return CMRoomDataLocations.PLAYER_SPAWNS.apply(server);
    }

    @Override
    public Codec<SpawnManager> codec() {
        return CODEC;
    }

    private RoomInstance getRoomInstance() {
        if (roomInstance == null) {
            final var instance = CompactMachines.room(this.roomCode).orElseThrow();
            this.roomInstance = instance;
        }

        return this.roomInstance;
    }

    private record RoomSpawns(RoomSpawn defaultSpawn,
                              Map<UUID, RoomSpawn> playerSpawnsSnapshot) implements IRoomSpawns {

        @Override
        public Optional<IRoomSpawn> forPlayer(UUID player) {
            return Optional.ofNullable(playerSpawnsSnapshot.get(player));
        }
    }
}
