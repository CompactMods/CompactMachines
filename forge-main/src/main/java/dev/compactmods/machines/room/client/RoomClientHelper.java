package dev.compactmods.machines.room.client;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.room.Rooms;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;

public class RoomClientHelper {
    public static Registry<RoomTemplate> getTemplates() {
        return Minecraft.getInstance().level.registryAccess().registryOrThrow(Rooms.TEMPLATE_REG_KEY);
    }
}
