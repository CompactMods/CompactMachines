package org.dave.cm2.world.tools;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.tile.TileEntityMachine;
import org.dave.cm2.utility.Logz;
import org.dave.cm2.world.WorldSavedDataMachines;

public class StructureTools {
    public static int getCoordsForPos(BlockPos pos) {
        return getCoordsForPos(pos.getX());
    }

    public static int getCoordsForPos(int x) {
        return x >> 10;
    }

    public static EnumFacing getNextDirection(EnumFacing in) {
        switch(in) {
            case DOWN: return EnumFacing.UP;
            case UP: return EnumFacing.NORTH;
            case NORTH: return EnumFacing.SOUTH;
            case SOUTH: return EnumFacing.WEST;
            case WEST: return EnumFacing.EAST;
            case EAST: return null;
            default: return null;
        }
    }


    public static void generateCubeForMachine(TileEntityMachine machine) {
        if(machine.coords != -1) {
            return;
        }

        machine.coords = WorldSavedDataMachines.reserveMachineId();
        Logz.info("Reserved id %d for machine", machine.coords);
        machine.markDirty();

        StructureTools.generateCube(machine);
    }

    private static void generateCube(TileEntityMachine machine) {
        int startX = machine.coords * 1024;
        int startY = 40;
        int startZ = 0;
        int size = machine.getSize().getDimension();

        StructureTools.generateCube(startX, startY, startZ, startX + size, startY + size, startZ + size);
    }

    private static void generateCube(int posX1, int posY1, int posZ1, int posX2, int posY2, int posZ2) {
        WorldServer machineWorld = DimensionTools.getServerMachineWorld();

        int minX = Math.min(posX1, posX2);
        int minY = Math.min(posY1, posY2);
        int minZ = Math.min(posZ1, posZ2);

        int maxX = Math.max(posX1, posX2);
        int maxY = Math.max(posY1, posY2);
        int maxZ = Math.max(posZ1, posZ2);

        IBlockState state = Blockss.wall.getDefaultState();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || y == minY || z == minZ || x == maxX || y == maxY || z == maxZ) {
                        BlockPos pos = new BlockPos(x, y, z);
                        machineWorld.setBlockState(pos, state);
                    }
                }
            }
        }
    }

    public static EnumFacing getInsetWallFacing(BlockPos wallPos, int size) {
        int x = wallPos.getX() % 1024;
        int y = wallPos.getY();
        int z = wallPos.getZ();

        if(y == 40) {
            return EnumFacing.UP;
        } else if(y == 40 + size) {
            return EnumFacing.DOWN;
        } else if(x == 0) {
            return EnumFacing.EAST;
        } else if(x == 0 + size) {
            return EnumFacing.WEST;
        } else if(z == 0) {
            return EnumFacing.SOUTH;
        } else {
            return EnumFacing.NORTH;
        }
    }

}
