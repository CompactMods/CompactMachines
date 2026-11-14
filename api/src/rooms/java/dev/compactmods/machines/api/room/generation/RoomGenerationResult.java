package dev.compactmods.machines.api.room.generation;

public record RoomGenerationResult(String newCode,
                                   dev.compactmods.machines.api.room.spatial.RoomBoundaries newRoomBoundaries) {
}
