package dev.compactmods.machines.api.room.exceptions;

public class NonexistentRoomException extends Throwable {
    private final String room;

    public NonexistentRoomException(String room) {
        super("The requested room could not be found.");
        this.room = room;
    }

    public String getRoom() {
        return room;
    }
}
