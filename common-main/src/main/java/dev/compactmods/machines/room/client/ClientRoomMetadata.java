package dev.compactmods.machines.room.client;

import dev.compactmods.machines.api.room.IPlayerRoomMetadata;

import java.util.UUID;

public record ClientRoomMetadata(String roomCode, UUID owner)
        implements IPlayerRoomMetadata {
}
