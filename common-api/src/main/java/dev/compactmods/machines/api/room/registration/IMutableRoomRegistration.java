package dev.compactmods.machines.api.room.registration;

import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public interface IMutableRoomRegistration extends IRoomRegistration {
    IMutableRoomRegistration setSpawnPosition(Vec3 spawnPosition);
    IMutableRoomRegistration setSpawnRotation(Vec2 spawnRotation);
}
