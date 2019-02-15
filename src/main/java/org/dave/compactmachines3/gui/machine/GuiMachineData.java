package org.dave.compactmachines3.gui.machine;

import net.minecraft.entity.player.EntityPlayer;
import org.dave.compactmachines3.utility.DimensionBlockPos;

import java.util.ArrayList;
import java.util.Set;

public class GuiMachineData {
    public static int machineSize;
    public static int coords;

    public static DimensionBlockPos machinePos;
    public static String owner;
    public static String customName;
    public static ArrayList<String> playerWhiteList;
    public static boolean locked;

    public static boolean requiresNewDisplayList = false;
    public static boolean canRender = false;

    public static boolean isAllowedToEnter(EntityPlayer player) {
        if(!locked) {
            return true;
        }

        if(owner == null || owner.length() <= 0) {
            return true;
        }

        if(player.getName().equals(owner)) {
            return true;
        }

        if(playerWhiteList.contains(player.getName())) {
            return true;
        }

        return false;
    }

    public static void updateGuiMachineData(int machineSize, int coords, DimensionBlockPos machinePos, String owner, String customName, Set<String> playerWhiteList, boolean locked) {
        canRender = false;
        GuiMachineData.machineSize = machineSize;
        GuiMachineData.coords = coords;
        GuiMachineData.machinePos = machinePos;
        GuiMachineData.owner = owner;
        GuiMachineData.customName = customName;
        GuiMachineData.playerWhiteList = new ArrayList<>(playerWhiteList);
        GuiMachineData.locked = locked;

        requiresNewDisplayList = true;
        canRender = true;
    }
}
