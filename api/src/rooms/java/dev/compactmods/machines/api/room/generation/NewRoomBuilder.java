package dev.compactmods.machines.api.room.generation;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import net.minecraft.core.Holder;

import java.util.UUID;

public interface NewRoomBuilder {

    NewRoomBuilder template(Holder<RoomTemplate> template);

    NewRoomBuilder template(RoomTemplate template);

    NewRoomBuilder owner(UUID owner);

    RoomGenerationDetails build();
}
