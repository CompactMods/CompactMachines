package dev.compactmods.machines.api.room.registration;

import dev.compactmods.machines.api.room.IRoomOwnerLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface IRoomRegistration extends IBasicRoomInfo {

    Vec3i dimensions();

    UUID owner(IRoomOwnerLookup lookup);

    Vec3 center();

    AABB innerBounds();

    AABB outerBounds();

    Vec3 spawnPosition(IRoomSpawnLookup spawns);

    Vec2 spawnRotation(IRoomSpawnLookup spawns);

    Optional<ResourceLocation> getTemplate();

    Stream<ChunkPos> chunks();
}
