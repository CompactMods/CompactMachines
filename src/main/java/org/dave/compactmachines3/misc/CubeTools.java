package org.dave.compactmachines3.misc;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.utility.ChunkBlockAccess;
import org.dave.compactmachines3.utility.Logz;

public class CubeTools {
    public static int getCubeSize(IBlockAccess world, int coord) {
        int yOffset = 40;
        if(world instanceof ChunkBlockAccess) {
            yOffset = 0;
        }

        BlockPos base = new BlockPos(coord * 1024, yOffset, 0);
        if(world.getBlockState(base.add(14,0,0)).getBlock() == Blockss.wall) {
            return 5;
        }

        if(world.getBlockState(base.add(12,0,0)).getBlock()  == Blockss.wall) {
            return 4;
        }

        if(world.getBlockState(base.add(10,0,0)).getBlock()  == Blockss.wall) {
            return 3;
        }

        if(world.getBlockState(base.add(8,0,0)).getBlock()  == Blockss.wall) {
            return 2;
        }

        if(world.getBlockState(base.add(6,0,0)).getBlock()  == Blockss.wall) {
            return 1;
        }

        return 0;
    }

    public static boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side) {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        int yOffset = 40;
        if(world instanceof ChunkBlockAccess) {
            yOffset = 0;
        }

        int coord = x / 1024;
        int size = EnumMachineSize.getFromMeta(CubeTools.getCubeSize(world, coord)).getDimension();

        int relativeX = x - (coord * 1024);

        // Bottom layer
        if((y == yOffset) && relativeX > 0 && relativeX < size && z < size && z > 0) {
            if(side == EnumFacing.UP) {
                return true;
            }
        }

        if(y == yOffset+size && relativeX > 0 && relativeX < size && z < size && z > 0) {
            if(side == EnumFacing.DOWN) {
                return true;
            }
        }

        if(y > yOffset && y < yOffset+size) {
            if(side == EnumFacing.EAST && relativeX == 0 && z < size && z > 0) {
                return true;
            }

            if(side == EnumFacing.WEST && relativeX == size && z < size && z > 0) {
                return true;
            }

            if(side == EnumFacing.NORTH && z == size && relativeX < size && relativeX > 0) {
                return true;
            }

            if(side == EnumFacing.SOUTH && z == 0 && relativeX < size && relativeX > 0) {
                return true;
            }

        }

        return false;
    }
}
