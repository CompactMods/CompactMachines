package dev.compactmods.machines.forge.room.client;

import dev.compactmods.machines.forge.room.RoomHelper;
import dev.compactmods.machines.forge.room.ui.MachineRoomScreen;
import dev.compactmods.machines.room.client.ClientRoomMetadata;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.UUID;

public class ClientRoomPacketHandler {
    public static void handleBlockData(StructureTemplate blocks) {
        final var mc = Minecraft.getInstance();
        if(mc.screen instanceof MachineRoomScreen mrs) {
            mrs.getMenu().setBlocks(blocks);
            mrs.updateBlockRender();
        }
    }

    public static void handleRoomSync(String roomCode, UUID owner) {
        final var mc = Minecraft.getInstance();
        mc.player.getCapability(RoomHelper.CURRENT_ROOM_META).ifPresent(meta -> {
            if(owner.equals(Util.NIL_UUID))
                meta.clearCurrent();
            else
                meta.setCurrent(new ClientRoomMetadata(roomCode, owner));
        });
    }
}
