package dev.compactmods.machines.api.room;

import java.util.Optional;
import java.util.UUID;

public interface IPlayerRoomMetadataProvider {
    Optional<IPlayerRoomMetadata> currentRoom();
    Optional<String> roomCode();
    Optional<UUID> owner();
    void clearCurrent();
    void setCurrent(IPlayerRoomMetadata current);
}
