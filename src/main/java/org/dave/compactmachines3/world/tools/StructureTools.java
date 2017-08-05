package org.dave.compactmachines3.world.tools;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.schema.BlockInformation;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

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
        IBlockState state = Blockss.wall.getDefaultState();
        StructureTools.generateCube(machineWorld, new BlockPos(startX, startY, startZ), size, state);
    }

    public static void generateCube(World world, BlockPos cornerPos, int size, IBlockState state) {
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

    public static List<BlockInformation> getSchema(int coords) {
        TileEntity machine = WorldSavedDataMachines.INSTANCE.getMachinePosition(coords).getTileEntity();
        if(machine != null && machine instanceof TileEntityMachine) {
            return getSchema((TileEntityMachine) machine);
        }

        return null;
    }

    public static List<BlockInformation> getSchema(TileEntityMachine machine) {
        List<BlockInformation> blockList = new ArrayList<>();

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        int size = machine.getSize().getDimension();

        int startX = machine.coords * 1024 + size;
        int startY = 40 + size;
        int startZ = size;

        for (int x = 1; x <= size-1; x++) {
            for (int y = 1; y <= size-1; y++) {
                for (int z = 1; z <= size-1; z++) {
                    BlockPos absolutePos = new BlockPos(startX - x, startY - y, startZ - z);
                    BlockPos relativePos = new BlockPos(x-1, y-1, z-1);

                    if(!machineWorld.isAirBlock(absolutePos)) {
                        IBlockState state = machineWorld.getBlockState(absolutePos);
                        Block block = state.getBlock();
                        NBTTagCompound nbt = null;
                        boolean writePositionData = false;
                        if(block.hasTileEntity(state)) {
                            TileEntity te = machineWorld.getTileEntity(absolutePos);
                            nbt = new NBTTagCompound();
                            te.writeToNBT(nbt);

                            boolean storedX = nbt.hasKey("x") && nbt.getInteger("x") == absolutePos.getX();
                            boolean storedY = nbt.hasKey("y") && nbt.getInteger("y") == absolutePos.getY();
                            boolean storedZ = nbt.hasKey("z") && nbt.getInteger("z") == absolutePos.getZ();

                            if(storedX && storedY && storedZ) {
                                writePositionData = true;
                            }
                        }
                        blockList.add(new BlockInformation(relativePos, block, block.getMetaFromState(state), nbt, writePositionData));
                    }
                }
            }
        }

        return blockList;
    }
}
