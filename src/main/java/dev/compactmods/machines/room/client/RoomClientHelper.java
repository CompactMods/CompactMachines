package dev.compactmods.machines.room.client;

import dev.compactmods.machines.api.core.CMRegistries;
import dev.compactmods.machines.api.room.RoomTemplate;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;

public class RoomClientHelper {

    public static Registry<RoomTemplate> getTemplates() {
        return Minecraft.getInstance().level.registryAccess()
                .registryOrThrow(CMRegistries.TEMPLATE_REG_KEY);
    }
}
