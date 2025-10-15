package dev.compactmods.machines.client.room;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.client.KeyMapping;

public interface RoomKeyMappings {
    KeyMapping.Category CATEGORY = new KeyMapping.Category(CompactMachines.modRL("general"));
}
