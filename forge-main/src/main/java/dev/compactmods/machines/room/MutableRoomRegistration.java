package dev.compactmods.machines.room;

import dev.compactmods.machines.api.room.IRoomLookup;
import dev.compactmods.machines.api.room.IRoomOwnerLookup;
import dev.compactmods.machines.api.room.registration.IMutableRoomRegistration;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.api.room.registration.IRoomSpawnLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class MutableRoomRegistration implements IMutableRoomRegistration {
    private final IRoomLookup lookup;
    private final IRoomRegistration currentData;

    public MutableRoomRegistration(IRoomLookup lookup, IRoomRegistration currentRoomData) {
        this.lookup = lookup;
        this.currentData = currentRoomData;
    }

    @Override
    public IMutableRoomRegistration setSpawnPosition(Vec3 spawnPosition) {
        return this;
    }

    @Override
    public IMutableRoomRegistration setSpawnRotation(Vec2 spawnRotation) {
        return this;
    }

    @Override
    public String code() {
        return currentData.code();
    }

    @Override
    public Vec3i dimensions() {
        return currentData.dimensions();
    }

    @Override
    public UUID owner(IRoomOwnerLookup lookup) {
        return lookup.getRoomOwner(currentData.code()).orElseThrow();
    }

    @Override
    public Vec3 center() {
        return currentData.center();
    }

    @Override
    public AABB innerBounds() {
        return currentData.innerBounds();
    }

    @Override
    public AABB outerBounds() {
        return currentData.outerBounds();
    }

    @Override
    public Vec3 spawnPosition(IRoomSpawnLookup spawns) {
        return currentData.spawnPosition(spawns);
    }

    @Override
    public Vec2 spawnRotation(IRoomSpawnLookup spawns) {
        return currentData.spawnRotation(spawns);
    }

    @Override
    public Optional<ResourceLocation> getTemplate() {
        return currentData.getTemplate();
    }

    @Override
    public Stream<ChunkPos> chunks() {
        return null;
    }

    @Override
    public int color() {
        return currentData.color();
    }
}
