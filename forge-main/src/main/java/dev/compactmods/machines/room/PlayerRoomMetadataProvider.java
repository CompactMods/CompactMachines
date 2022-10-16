package dev.compactmods.machines.room;

import dev.compactmods.machines.api.room.IPlayerRoomMetadata;
import dev.compactmods.machines.api.room.IPlayerRoomMetadataProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class PlayerRoomMetadataProvider implements IPlayerRoomMetadataProvider {

    @Nullable
    private IPlayerRoomMetadata currentRoom;

    private final LazyOptional<CurrentRoomData> lazy;

    public PlayerRoomMetadataProvider() {
        this.lazy = LazyOptional.of(this::lazyRoom).cast();
    }

    private IPlayerRoomMetadata lazyRoom() {
        return currentRoom;
    }

    public LazyOptional<CurrentRoomData> getRoomLazy() {
        if(currentRoom == null)
            return LazyOptional.empty();

        return lazy;
    }

    @Override
    public Optional<IPlayerRoomMetadata> currentRoom() {
        return Optional.ofNullable(currentRoom);
    }

    @Override
    public Optional<String> roomCode() {
        if(currentRoom == null)
            return Optional.empty();

        return Optional.of(currentRoom.roomCode());
    }

    @Override
    public Optional<UUID> owner() {
        if(currentRoom == null) return Optional.empty();
        return Optional.of(currentRoom.owner());
    }

    @Override
    public void clearCurrent() {
        this.currentRoom = null;
        this.lazy.invalidate();
    }

    @Override
    public void setCurrent(IPlayerRoomMetadata current) {
        this.currentRoom = current;
        this.lazy.invalidate();
    }

    public record CurrentRoomData(String roomCode, UUID owner) implements IPlayerRoomMetadata {}
}
