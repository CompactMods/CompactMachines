package dev.compactmods.machines.room.network;

import dev.compactmods.machines.room.client.MachineRoomScreen;
import dev.compactmods.machines.room.menu.MachineRoomMenu;
import net.minecraft.client.Minecraft;

public class ClientRoomNetworkHandler {
    public static void handleBlockData(InitialRoomBlockDataPacket blockData) {
        final var mc = Minecraft.getInstance();
        if(mc.screen instanceof MachineRoomScreen mrs) {
            mrs.getMenu().setBlocks(blockData.blocks());
        }
    }
}
