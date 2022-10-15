package dev.compactmods.machines.api.room;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

import java.util.Optional;
import java.util.UUID;

@AutoRegisterCapability
public interface IPlayerRoomMetadataProvider {
    Optional<IPlayerRoomMetadata> currentRoom();
    Optional<String> roomCode();
    Optional<UUID> owner();
    void clearCurrent();
    void setCurrent(IPlayerRoomMetadata current);
}
