package org.dave.compactmachines3.world.tools;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.schema.BlockInformation;
import org.dave.compactmachines3.schema.Schema;
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

    public static void setBiomeForCoords(int coords, Biome biome) {
        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        Chunk chunk = machineWorld.getChunk((coords << 10) >> 4, 0);
        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                chunk.getBiomeArray()[z << 4 | x] = (byte)Biome.getIdForBiome(biome);
            }
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
        IBlockState barrier = Blocks.BARRIER.getDefaultState();
        StructureTools.generateCube(machineWorld, new BlockPos(startX, startY, startZ), size, state);
        StructureTools.generateCube(machineWorld, new BlockPos(startX+1, startY+1, startZ+1), size+2, barrier);
        StructureTools.generateCube(machineWorld, new BlockPos(startX+2, startY+2, startZ+2), size+4, barrier);
        StructureTools.generateCube(machineWorld, new BlockPos(startX+3, startY+3, startZ+3), size+6, barrier);
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
                    if (x == minX || z == minZ || x == maxX || y == maxY || z == maxZ || y == minY) {
                        if(!includeFloor && y == minY) {
                            continue;
                        }

                        BlockPos pos = new BlockPos(x, y, z);
                        list.add(pos);
                    }
                }
            }
        }

        return list;
    }

    public static List<BlockInformation> createNewSchema(int coords) {
        TileEntity machine = WorldSavedDataMachines.INSTANCE.getMachinePosition(coords).getTileEntity();
        if(machine != null && machine instanceof TileEntityMachine) {
            return createNewSchema((TileEntityMachine) machine);
        }

        return null;
    }

    public static List<BlockInformation> createNewSchema(TileEntityMachine machine) {
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

    public static void restoreSchema(Schema schema, int coords) {
        List<BlockInformation> blockList = schema.getBlocks();

        TileEntity te = WorldSavedDataMachines.INSTANCE.getMachinePosition(coords).getTileEntity();

        if(te != null && te instanceof TileEntityMachine) {
            WorldServer machineWorld = DimensionTools.getServerMachineWorld();

            TileEntityMachine machine = (TileEntityMachine) te;
            int size = machine.getSize().getDimension();
            int startX = machine.coords * 1024 + size - 1;
            int startY = 40 + size - 1;
            int startZ = 0 + size - 1;

            for(BlockInformation bi : blockList) {
                BlockPos absolutePos = new BlockPos(
                        startX - bi.position.getX(),
                        startY - bi.position.getY(),
                        startZ - bi.position.getZ()
                );

                IBlockState state = bi.block.getStateFromMeta(bi.meta);
                machineWorld.setBlockState(absolutePos, state);

                if(bi.nbt != null) {
                    TileEntity restoredTe = machineWorld.getTileEntity(absolutePos);
                    if (restoredTe == null) {
                        restoredTe = bi.block.createTileEntity(machineWorld, state);
                    }

                    if(bi.writePositionData) {
                        bi.nbt.setInteger("x", absolutePos.getX());
                        bi.nbt.setInteger("y", absolutePos.getY());
                        bi.nbt.setInteger("z", absolutePos.getZ());
                    }

                    restoredTe.readFromNBT(bi.nbt);
                    restoredTe.markDirty();
                }
            }
        }
    }
}
