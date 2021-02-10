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
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.schema.BlockInformation;
import org.dave.compactmachines3.schema.Schema;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.world.WorldSavedDataMachines;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StructureTools {
    public static int getIdForPos(BlockPos pos) {
        return getIdForPos(pos.getX(), pos.getY(), pos.getZ());
    }

    public static int getIdForPos(int x, int y, int z) {
        return WorldSavedDataMachines.getInstance().getMachineIdFromBoxPos(x, y, z);
    }

    public static EnumFacing getNextDirection(EnumFacing in) {
        if (in == null) {
            return null;
        }

        int next = in.getIndex() + 1;
        if (next == EnumFacing.EAST.getIndex() + 1) {
            return null;
        }

        return EnumFacing.byIndex(next);
    }

    public static boolean setBiomeForMachineId(int id, Biome biome) {
        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        if (machineWorld == null)
            return false;
        BlockPos roomPos = WorldSavedDataMachines.getInstance().getMachineRoomPosition(id);
        if (roomPos == null) {
            return false;
        }
        Chunk chunk = machineWorld.getChunk(roomPos);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.getBiomeArray()[z << 4 | x] = (byte) Biome.getIdForBiome(biome);
            }
        }
        return true;
    }

    public static void generateCubeForMachine(TileEntityMachine machine) {
        if (machine.id != -1) {
            return;
        }

        machine.id = WorldSavedDataMachines.reserveMachineId();
        CompactMachines3.logger.info("Reserved id {} for machine", machine.id);
        machine.markDirty();

        StructureTools.generateCube(machine);
    }

    private static void generateCube(TileEntityMachine machine) {
        int size = machine.getSize().getDimension();
        WorldSavedDataMachines wsd = WorldSavedDataMachines.getInstance();
        Map<Integer, BlockPos> machineGrid = wsd.machineGrid;
        BlockPos roomPos = null;

        do {
            roomPos = getNextGridPosition(roomPos); // Use the previous roomPos or null to generate the next one, see method docs
        } while (machineGrid.containsValue(roomPos)); // Ensures there are no duplicate grid positions

        machine.setRoomPos(roomPos);
        wsd.addMachineSize(machine.id, machine.getSize());
        machine.markDirty();
        int startX = machine.getRoomPos().getX();
        int startY = machine.getRoomPos().getY();
        int startZ = machine.getRoomPos().getZ();

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        if (machineWorld == null)
            return;
        IBlockState state = Blockss.wall.getDefaultState();
        IBlockState barrier = Blocks.BARRIER.getDefaultState();
        StructureTools.generateCube(machineWorld, new BlockPos(startX, startY, startZ), size, state);
        StructureTools.generateCube(machineWorld, new BlockPos(startX - 1, startY - 1, startZ - 1), size + 2, barrier);
        StructureTools.generateCube(machineWorld, new BlockPos(startX - 2, startY - 2, startZ - 2), size + 4, barrier);
        StructureTools.generateCube(machineWorld, new BlockPos(startX - 3, startY - 3, startZ - 3), size + 6, barrier);
    }

    /**
     * Gets the next available grid position offset from {@code previous},
     * or uses {@link WorldSavedDataMachines#lastGrid} if {@code previous} is null.
     * {@code lastGrid} is the last grid point of the newest machine.
     *
     * @param previous The previous grid position to use for calculation, or null to use the last grid position from {@link WorldSavedDataMachines#lastGrid}.
     * @return A grid position based on the northwest bottom-most corner of the cube. If this is the first machine, starts at (0, 40, 0).
     * The positions are generated based on a grid pattern made by clockwise positions around the origin (0, 40, 0) at 512-block intervals.
     * @see <a href="https://github.com/tastybento/askyblock/blob/master/src/com/wasteofplastic/askyblock/commands/IslandCmd.java#L892-L919">The original grid generation function</a>
     */
    // I had no idea how to go about this so I used code from a SkyBlock plugin to make the grid generation function.
    // see https://github.com/tastybento/askyblock/blob/master/src/com/wasteofplastic/askyblock/commands/IslandCmd.java#L892-L919
    private static BlockPos getNextGridPosition(BlockPos previous) {
        WorldSavedDataMachines wsd = WorldSavedDataMachines.getInstance();
        // Get last grid point
        BlockPos lastGrid = previous != null ? previous : wsd.lastGrid;

        if (lastGrid == null) { // If there was legacy data, this will be null and the generation system will do the rest
            lastGrid = new BlockPos(0, 40, 0); // Since this is the first ever grid value, all other grid positions will always have y=40
            return lastGrid;
        }

        int x = lastGrid.getX();
        int z = lastGrid.getZ();
        int distance = 512; // This is half of the legacy value but shouldn't be that big anyways. Probably do not change this

        if (x < z) {
            if (-1 * x < z) {
                return lastGrid.add(distance, 0, 0);
            }

            return lastGrid.add(0, 0, distance);
        } else if (x > z) {
            if (-1 * x >= z) {
                return lastGrid.add(-distance, 0, 0);
            }

            return lastGrid.add(0, 0, -distance);
        } else if (x <= 0) {
            return lastGrid.add(0, 0, distance);
        } else {
            return lastGrid.add(0, 0, -distance);
        }
    }

    public static void generateCube(World world, BlockPos cornerPos, int size, IBlockState state) {
        for (BlockPos pos : getCubePositions(cornerPos, size, size, size)) {
            world.setBlockState(pos, state);
        }
    }

    /**
     * Get the {@link EnumFacing} direction from a {@code wallPos} relative to a {@code roomPos} and its {@code size}.
     * The returned block face points towards the inside of the room, e.g. a block in the floor would return {@link EnumFacing#UP}.
     * Useful for getting the block touching a tunnel.
     *
     * @param wallPos The {@link BlockPos} of the wall.
     * @param roomPos The {@link BlockPos} of the room.
     * @param size The size of the room.
     * @return A block face pointing towards the inside of the room.
     */
    public static EnumFacing getInsetWallFacing(BlockPos wallPos, BlockPos roomPos, EnumMachineSize size) {
        int dimensions = size.getDimension();
        int x = wallPos.getX() - roomPos.getX();
        int y = wallPos.getY() - roomPos.getY();
        int z = wallPos.getZ() - roomPos.getZ();

        if (y == 0) {
            return EnumFacing.UP;
        } else if (y == dimensions) {
            return EnumFacing.DOWN;
        } else if (x == 0) {
            return EnumFacing.EAST;
        } else if (x == dimensions) {
            return EnumFacing.WEST;
        } else if (z == 0) {
            return EnumFacing.SOUTH;
        } else {
            return EnumFacing.NORTH;
        }
    }

    public static List<BlockPos> getCubePositionsLegacy(BlockPos cornerPos, int width, int height, int depth, boolean includeFloor) {
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

    public static List<BlockPos> getCubePositions(BlockPos bottomNorthwestCorner, int width, int height, int depth) {
        List<BlockPos> list = new ArrayList<>();
        for (int x = 0; x <= width; x++) {
            for (int y = 0; y <= height; y++) {
                for (int z = 0; z <= depth; z++) {
                    if (x == 0 || x == width || y == 0 || y == height || z == 0 || z == depth) {
                        BlockPos pos = new BlockPos(x, y, z).add(bottomNorthwestCorner);
                        list.add(pos);
                    }
                }
            }
        }

        return list;
    }

    public static List<BlockInformation> createNewSchema(int id) {
        TileEntity machine = WorldSavedDataMachines.getInstance().getMachineBlockPosition(id).getTileEntity();
        if (machine instanceof TileEntityMachine) { // instanceof returns false on null
            return createNewSchema((TileEntityMachine) machine);
        }

        return null;
    }

    public static List<BlockInformation> createNewSchema(TileEntityMachine machine) {
        List<BlockInformation> blockList = new ArrayList<>();

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        if (machineWorld == null)
            return blockList;
        int size = machine.getSize().getDimension();

        // Southeast corner in the top block, opposite corner of the original roomPos
        BlockPos roomPos = machine.getRoomPos().add(size, size, size);

        for (int x = 1; x <= size - 1; x++) {
            for (int y = 1; y <= size - 1; y++) {
                for (int z = 1; z <= size - 1; z++) {
                    BlockPos absolutePos = roomPos.add(-x, -y, -z);
                    BlockPos relativePos = new BlockPos(x - 1, y - 1, z - 1);

                    if (!machineWorld.isAirBlock(absolutePos)) {
                        IBlockState state = machineWorld.getBlockState(absolutePos);
                        Block block = state.getBlock();
                        NBTTagCompound nbt = null;
                        boolean writePositionData = false;
                        TileEntity te = machineWorld.getTileEntity(absolutePos);
                        if (block.hasTileEntity(state) && te != null) {
                            nbt = new NBTTagCompound();
                            te.writeToNBT(nbt);

                            boolean storedX = nbt.hasKey("x") && nbt.getInteger("x") == absolutePos.getX();
                            boolean storedY = nbt.hasKey("y") && nbt.getInteger("y") == absolutePos.getY();
                            boolean storedZ = nbt.hasKey("z") && nbt.getInteger("z") == absolutePos.getZ();

                            if (storedX && storedY && storedZ) {
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

    public static void restoreSchema(Schema schema, int id) {
        List<BlockInformation> blockList = schema.getBlocks();

        TileEntityMachine machine = WorldSavedDataMachines.getInstance().getMachine(id);

        if (machine == null)
            return;

        WorldServer machineWorld = DimensionTools.getServerMachineWorld();
        if (machineWorld == null)
            return;
        int size = machine.getSize().getDimension();

        // Southeast top corner of the **air** cube inside the machine.
        BlockPos roomPos = machine.getRoomPos().add(size - 1, size - 1, size - 1);

        for (BlockInformation bi : blockList) {
            BlockPos absolutePos = roomPos.add(-bi.position.getX(), -bi.position.getY(), -bi.position.getZ());

            IBlockState state = bi.block.getStateFromMeta(bi.meta);
            machineWorld.setBlockState(absolutePos, state);

            if (bi.nbt != null) {
                TileEntity restoredTe = machineWorld.getTileEntity(absolutePos);
                if (restoredTe == null) {
                    restoredTe = bi.block.createTileEntity(machineWorld, state);
                }

                if (restoredTe == null) {
                    continue; // somehow this was still null, catch
                }

                if (bi.writePositionData) {
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
