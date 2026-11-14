package dev.compactmods.machines.shrinking.network.client;

import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ClientShrinkingPacketHandler {

    public static void handleRoomSync(String roomCode) {
        final var mc = Minecraft.getInstance();

        assert mc.player != null;
        mc.player.setData(Shrinking.CURRENT_ROOM_CODE, roomCode);
    }
}
