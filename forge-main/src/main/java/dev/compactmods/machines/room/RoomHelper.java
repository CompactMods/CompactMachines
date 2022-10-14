package dev.compactmods.machines.room;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.room.client.RoomClientHelper;
import dev.compactmods.machines.room.server.RoomServerHelper;
import net.minecraft.core.Registry;
import net.minecraftforge.fml.DistExecutor;

public class RoomHelper {

    public static Registry<RoomTemplate> getTemplates() {
        return DistExecutor.safeRunForDist(() -> RoomClientHelper::getTemplates, () -> RoomServerHelper::getTemplates);
    }
}
