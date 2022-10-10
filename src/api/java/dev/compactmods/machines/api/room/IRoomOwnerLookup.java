package dev.compactmods.machines.api.room;

import java.util.Optional;
import java.util.UUID;

public interface IRoomOwnerLookup {

    Optional<UUID> getRoomOwner(String roomCode);
}
