package dev.compactmods.machines.shrinking.api.history;

public enum RoomEntryResult {
    SUCCESS(true),
    FAILED_TOO_FAR_DOWN(false),
    FAILED_ROOM_INVALID(false);

    private final boolean success;

    RoomEntryResult(boolean successful) {
        this.success = successful;
    }

    public boolean successful() {
        return success;
    }
}
