package dev.compactmods.machines.room.spawn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.spawn.IRoomSpawnManager;
import dev.compactmods.machines.api.room.spawn.IRoomSpawns;
import dev.compactmods.machines.api.room.spawn.RoomSpawn;
import dev.compactmods.machines.core.data.Saveable;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * A room's spawn points. Persisted by its owning {@link ServerRoomSpawnManagers}, which binds
 * {@link #onChanged} when it hands the manager out: every spawn change is written straight away.
 */
public class SpawnManager implements IRoomSpawnManager, Saveable {

    private static final UnboundedMapCodec<UUID, RoomSpawn> PLAYER_SPAWNS_CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, RoomSpawn.CODEC);
    public static final Codec<SpawnManager> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("roomCode").forGetter(x -> x.roomCode),
            PLAYER_SPAWNS_CODEC.fieldOf("player_spawns").forGetter(x -> x.playerSpawns),
            RoomSpawn.CODEC.fieldOf("default_spawn").forGetter(x -> x.defaultSpawn),
            RoomBoundaries.MAP_CODEC.fieldOf("room_bounds").forGetter(x -> x.roomBoundaries)
    ).apply(inst, SpawnManager::new));

    private final String roomCode;
    private final RoomBoundaries roomBoundaries;
    private RoomSpawn defaultSpawn;
    private final Map<UUID, RoomSpawn> playerSpawns;

    /** Persists this manager; bound by the owner (the codec can't carry it). */
    private Runnable onChanged = () -> {};

    SpawnManager(RoomInstance roomInstance) {
        this.roomCode = roomInstance.code();
        this.roomBoundaries = roomInstance.boundaries();
        this.defaultSpawn = new RoomSpawn(roomBoundaries.defaultSpawn(), Vec2.ZERO);
        this.playerSpawns = new Object2ReferenceLinkedOpenHashMap<>();
    }

    private SpawnManager(String roomCode, Map<UUID, RoomSpawn> playerSpawns, RoomSpawn defaultSpawn, RoomBoundaries roomBoundaries) {
        this.roomCode = roomCode;
        this.playerSpawns = new Object2ReferenceLinkedOpenHashMap<>(playerSpawns);
        this.defaultSpawn = defaultSpawn;
        this.roomBoundaries = roomBoundaries;
    }

    /** Called by the owner with how to persist this manager after a change. */
    void onChanged(Runnable onChanged) {
        this.onChanged = onChanged;
    }

    @Override
    public void resetPlayerSpawn(UUID player) {
        if (playerSpawns.remove(player) != null)
            onChanged.run();
    }

    @Override
    public void setDefaultSpawn(Vec3 position, Vec2 rotation) {
        defaultSpawn = new RoomSpawn(position, rotation);
        onChanged.run();
    }

    @Override
    public IRoomSpawns spawns() {
        final var ps = new HashMap<UUID, RoomSpawn>();
        playerSpawns.forEach(ps::putIfAbsent);
        return new RoomSpawns(defaultSpawn, ps);
    }

    @Override
    public void setPlayerSpawn(UUID player, Vec3 location, Vec2 rotation) {
        if (!roomBoundaries.innerBounds().contains(location))
            return;

        playerSpawns.put(player, new RoomSpawn(location, rotation));
        onChanged.run();
    }

    @Override
    public void save() {
        onChanged.run();
    }

    private record RoomSpawns(RoomSpawn defaultSpawn,
                              Map<UUID, RoomSpawn> playerSpawnsSnapshot) implements IRoomSpawns {

        @Override
        public Optional<RoomSpawn> forPlayer(UUID player) {
            return Optional.ofNullable(playerSpawnsSnapshot.get(player));
        }
    }
}
