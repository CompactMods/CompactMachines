package dev.compactmods.machines.room.client;

import dev.compactmods.machines.core.CompactMachinesCore;
import net.minecraft.client.KeyMapping;

public interface RoomKeyMappings {
    KeyMapping.Category CATEGORY = new KeyMapping.Category(CompactMachinesCore.identifier("general"));
}
