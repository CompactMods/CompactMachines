package org.dave.compactmachines3.misc;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.utility.ChunkBlockAccess;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

public class CubeTools {
    public static EnumMachineSize getCubeSize(IBlockAccess world, BlockPos roomPos) {
        for (EnumMachineSize size : EnumMachineSize.values()) {
            BlockPos pos = roomPos.add(size.getDimension(), 0, 0);
            if (world.getBlockState(pos).getBlock() == Blockss.wall) {
                return size;
            }
        }

        return null; // Not a valid cube size
    }

    public static boolean shouldSideBeRendered(IBlockAccess world, BlockPos blockPos, EnumFacing side) {
        // TODO: Remove this ChunkBlockAccess class from the mod if possible, investigate why it still exists?
        int yOffset = world instanceof ChunkBlockAccess ? 0 : 40;

        int id = CubeTools.getRoomId(blockPos);

        if (id == -1)
            return true; // Render as full block

        BlockPos roomPos = CompactMachines3.clientMachineGrid.get(id);
        EnumMachineSize sizeEnum = CompactMachines3.clientMachineSizes.get(id);

        if (sizeEnum == null)
            return true; // Render as full block

        int size = sizeEnum.getDimension();
        int x = blockPos.getX() - roomPos.getX(); // Relative x
        int y = blockPos.getY() - yOffset; // Relative y
        int z = blockPos.getZ() - roomPos.getZ(); // Relative z
        if (x != 0 && x != size && z != 0 && z != size && y > 0 && y < size) {
            return true; // Not part of the wall, render as full block
        }

        // NOTE: The EnumFacing rendering directions are the directions relative to the wall blocks, not the room!
        // This makes it easier to program and understand. It makes the direction the same as what the player would see when looking at the block in F3.
        EnumFacing lookingSide = side.getOpposite();

        if (lookingSide == EnumFacing.DOWN) { // Floor
            return y == 0 && x > 0 && x < size && z > 0 && z < size;
        } else if (lookingSide == EnumFacing.UP) { // Ceiling
            return y == size && x > 0 && x < size && z > 0 && z < size;
        } else if (y > 0 && y < size) { // Walls
            int xEdge = lookingSide.getXOffset() == -1 ? 0 : size;
            int zEdge = lookingSide.getZOffset() == -1 ? 0 : size;
            boolean validX = lookingSide.getAxis() == EnumFacing.Axis.X ? x == xEdge : x > 0 && x < size;
            boolean validZ = lookingSide.getAxis() == EnumFacing.Axis.Z ? z == zEdge : z > 0 && z < size;

            return validX && validZ;
        }

        return false;
    }

    public static int getRoomId(BlockPos blockPos) {
        if (CompactMachines3.clientMachineGrid == null || CompactMachines3.clientMachineSizes == null)
            return -1;
        return WorldSavedDataMachines.getMachineIdFromBoxPos(blockPos, CompactMachines3.clientMachineGrid, CompactMachines3.clientMachineSizes);
    }
}
