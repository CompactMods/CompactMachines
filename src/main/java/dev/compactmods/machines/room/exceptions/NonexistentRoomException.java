package dev.compactmods.machines.room.exceptions;

import net.minecraft.world.level.ChunkPos;

public class NonexistentRoomException extends Throwable {
    private final ChunkPos room;

    public NonexistentRoomException(ChunkPos room) {
        super("The requested room could not be found.");
        this.room = room;
    }

    public ChunkPos getRoom() {
        return room;
    }
}
