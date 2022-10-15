package dev.compactmods.machines.api.room;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface IPlayerRoomMetadata {
    @NotNull
    String roomCode();

    @NotNull
    UUID owner();
}
