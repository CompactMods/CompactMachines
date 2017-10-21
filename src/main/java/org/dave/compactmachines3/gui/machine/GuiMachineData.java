package org.dave.compactmachines3.gui.machine;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import org.dave.compactmachines3.utility.DimensionBlockPos;

public class GuiMachineData {
    public static NBTTagCompound rawData;
    public static Chunk chunk;
    public static int machineSize;
    public static int coords;

    public static DimensionBlockPos machinePos;
    public static String owner;
    public static String customName;
}
