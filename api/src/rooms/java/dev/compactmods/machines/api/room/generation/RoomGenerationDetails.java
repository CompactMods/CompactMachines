package dev.compactmods.machines.api.room.generation;

import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import net.minecraft.core.Holder;

import java.util.UUID;

public record RoomGenerationDetails(int spiralIndex, Holder<RoomTemplate> template,
                                    RoomBoundaries boundaries, UUID owner) {

}
