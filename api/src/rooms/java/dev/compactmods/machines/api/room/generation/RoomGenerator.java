package dev.compactmods.machines.api.room.generation;

import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.RoomInstance;

import java.util.Optional;

public interface RoomGenerator {

    NewRoomBuilder createNew() throws RoomGenerationException;

    Optional<RoomGenerationResult> generate(RoomGenerationDetails details) throws RoomGenerationException;
}
