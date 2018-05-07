package org.dave.compactmachines3.gui.machine;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import org.dave.compactmachines3.utility.ChunkUtils;
import org.dave.compactmachines3.utility.DimensionBlockPos;
import org.dave.compactmachines3.world.ProxyWorld;

import java.util.ArrayList;
import java.util.List;

public class GuiMachineData {
    public static NBTTagCompound rawData;
    public static int machineSize;
    public static int coords;

    public static DimensionBlockPos machinePos;
    public static String owner;
    public static String customName;

    public static Chunk chunk;
    public static ProxyWorld proxyWorld;

    public static List<BlockPos> toRender;

    public static boolean requiresNewDisplayList = false;
    public static boolean canRender = false;

    public static void updateGuiMachineData(NBTTagCompound rawData, int machineSize, int coords, DimensionBlockPos machinePos, String owner, String customName) {
        canRender = false;
        GuiMachineData.rawData = rawData;
        GuiMachineData.machineSize = machineSize;
        GuiMachineData.coords = coords;
        GuiMachineData.machinePos = machinePos;
        GuiMachineData.owner = owner;
        GuiMachineData.customName = customName;
        GuiMachineData.toRender = new ArrayList<>();

        proxyWorld = new ProxyWorld();
        GuiMachineData.chunk = ChunkUtils.readChunkFromNBT(proxyWorld, GuiMachineData.rawData);

        IBlockAccess blockAccess = ChunkUtils.getBlockAccessFromChunk(GuiMachineData.chunk);
        proxyWorld.setFakeWorld(blockAccess);

        for(int x = 15; x >= 0; x--) {
            for(int y = 15; y >= 0; y--) {
                for(int z = 15; z >= 0; z--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if(blockAccess.isAirBlock(pos)) {
                        continue;
                    }

                    toRender.add(pos);
                }
            }
        }

        requiresNewDisplayList = true;
        canRender = true;
    }
}
