package dev.compactmods.machines.api.room.registry;

import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/// Represents a room that has been registered with the room registry.
/// Contains the most basic information - the room code, which level it resides in, and where.
public interface RoomRegistration {

    String code();

    ServerLevel level();

    RoomBoundaries boundaries();

}
