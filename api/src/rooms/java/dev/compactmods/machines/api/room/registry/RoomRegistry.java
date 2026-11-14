package dev.compactmods.machines.api.room.registry;

import dev.compactmods.machines.api.room.generation.RoomGenerationDetails;
import dev.compactmods.machines.api.room.generation.RoomGenerator;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.core.data.Saveable;
import dev.compactmods.machines.api.room.RoomInstance;
import net.minecraft.core.Holder;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface RoomRegistry extends Saveable {

    boolean isRegistered(String room);

    Optional<RoomInstance> register(Holder<RoomTemplate> template, RoomBoundaries boundaries, UUID owner);

    Optional<RoomInstance> get(String room);

    int count();

    Stream<String> allRoomCodes();

    Stream<RoomInstance> allRooms();

}
