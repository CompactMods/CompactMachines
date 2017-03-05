package org.dave.cm2.world.tools;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.dave.cm2.init.Blockss;
import org.dave.cm2.tile.TileEntityMachine;
import org.dave.cm2.utility.Logz;
import org.dave.cm2.world.WorldSavedDataMachines;

import java.util.ArrayList;
import java.util.List;

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
        int size = machine.getSize().getDimension();
        int startX = machine.coords * 1024 + size;
        int startY = 40 + size;
        int startZ = size;

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        StructureTools.generateCube(machineWorld, new BlockPos(startX, startY, startZ), size);
    }

    private static void generateCube(World world, BlockPos cornerPos, int size) {
        IBlockState state = Blockss.wall.getDefaultState();
        for(BlockPos pos : getCubePositions(cornerPos, size+1, size+1, size+1, true)) {
            world.setBlockState(pos, state);
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

    public static List<BlockPos> getCubePositions(BlockPos cornerPos, int width, int height, int depth, boolean includeFloor) {
        int minX = Math.min(cornerPos.getX(), cornerPos.getX()-(width-1));
        int minY = Math.min(cornerPos.getY(), cornerPos.getY()-(height-1));
        int minZ = Math.min(cornerPos.getZ(), cornerPos.getZ()-(depth-1));

        int maxX = Math.max(cornerPos.getX(), cornerPos.getX()-(width-1));
        int maxY = Math.max(cornerPos.getY(), cornerPos.getY()-(height-1));
        int maxZ = Math.max(cornerPos.getZ(), cornerPos.getZ()-(depth-1));

        List<BlockPos> list = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (x == minX || z == minZ || x == maxX || y == maxY || z == maxZ || (y == minY && includeFloor)) {
                        BlockPos pos = new BlockPos(x, y, z);
                        list.add(pos);
                    }
                }
            }
        }

        return list;
    }
}
