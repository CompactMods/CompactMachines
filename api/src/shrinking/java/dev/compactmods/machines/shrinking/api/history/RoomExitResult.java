package dev.compactmods.machines.shrinking.api.history;

public enum RoomExitResult {
    SUCCESS_WENT_TO_LAST_ENTRYPOINT(true),
    SUCCESS_WENT_TO_SPAWN(true),
    FAILED_NOT_IN_COMPACT_DIM(false),
    FAILED_ROOM_NOT_FOUND(false);

    private final boolean success;

    RoomExitResult(boolean success) {
        this.success = success;
    }

    public boolean successful() {
        return this.success;
    }
}
