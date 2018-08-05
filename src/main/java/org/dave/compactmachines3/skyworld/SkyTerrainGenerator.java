package org.dave.compactmachines3.skyworld;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import org.dave.compactmachines3.block.BlockMachine;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.tile.TileEntityMachine;
import org.dave.compactmachines3.world.tools.StructureTools;

import java.util.List;

public class SkyTerrainGenerator {
    private final World world;
    private final SkyChunkGenerator chunkGenerator;

    // Make configurable?
    private final static int ROWS = 4;
    private final static int COLS = 4;

    public final static int ROOM_DIMENSION = 9;
    public final static int ROOM_PADDING = 16-ROOM_DIMENSION;
    public final static int ROOM_FLOOR_HEIGHT = 50;

    public final static IBlockState WALL_BLOCK = Blockss.wallBreakable.getDefaultState();

    public SkyTerrainGenerator(World world, SkyChunkGenerator skyChunkGenerator) {
        this.world = world;
        this.chunkGenerator = skyChunkGenerator;
    }

    public void generate(int chunkX, int chunkZ, ChunkPrimer cp) {
        if(isOutside(chunkX, chunkZ)) {
            return;
        }

        generateEmptyCube(chunkX, chunkZ, cp);
        generateCeilingWindows(chunkX, chunkZ, cp);
        generateConnections(chunkX, chunkZ, cp);
        generateLighting(chunkX, chunkZ, cp);
        generateMachine(chunkX, chunkZ, cp);
    }

    public void populate(int chunkX, int chunkZ) {
        if(isOutside(chunkX, chunkZ)) {
            return;
        }

        int center = (int) Math.floor(ROOM_DIMENSION / 2.0f);
        BlockPos machinePos = new BlockPos(15-center, ROOM_FLOOR_HEIGHT-ROOM_DIMENSION+2, 15-center);
        machinePos = machinePos.add(chunkX << 4, 0, chunkZ << 4);

        TileEntity te = world.getTileEntity(machinePos);

        if(!(te instanceof TileEntityMachine)) {
            return;
        }

        TileEntityMachine machine = (TileEntityMachine) te;
        machine.setSchema(chunkGenerator.config.schema.getName());
        machine.setLocked(chunkGenerator.config.startLocked);
        machine.markDirty();
    }

    private void generateMachine(int chunkX, int chunkZ, ChunkPrimer cp) {
        int center = (int) Math.floor(ROOM_DIMENSION / 2.0f);
        BlockPos machinePos = new BlockPos(15-center, ROOM_FLOOR_HEIGHT-ROOM_DIMENSION+2, 15-center);

        EnumMachineSize startSize = chunkGenerator.config.schema.getSize();
        cp.setBlockState(machinePos.getX(), machinePos.getY(), machinePos.getZ(), Blockss.machine.getDefaultState().withProperty(BlockMachine.SIZE, startSize));
    }

    private void generateLighting(int chunkX, int chunkZ, ChunkPrimer cp) {
        int meta = chunkZ * 4 + chunkX;
        int center = (int) Math.floor(ROOM_DIMENSION / 2.0f);

        BlockPos floorCenter = new BlockPos(15-center, ROOM_FLOOR_HEIGHT-ROOM_DIMENSION+1, 15-center);

        for(int x = -1; x <= 1; x++) {
            for(int z = -1; z <= 1; z++) {
                if(x == 0 && z == 0) {
                    continue;
                }

                BlockPos thisPos = floorCenter.add(x, 0, z);

                @SuppressWarnings("deprecation") IBlockState carpetState = Blocks.CARPET.getStateFromMeta(meta);
                cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ(), Blocks.GLOWSTONE.getDefaultState());
                cp.setBlockState(thisPos.getX(), thisPos.getY()+1, thisPos.getZ(), carpetState);
            }
        }

    }

    private void generateCeilingWindows(int chunkX, int chunkZ, ChunkPrimer cp) {
        int meta = chunkZ * 4 + chunkX;
        int center = (int) Math.floor(ROOM_DIMENSION / 2.0f);
        BlockPos ceilingCenter = new BlockPos(15-center, ROOM_FLOOR_HEIGHT, 15-center);
        for(int x = -2; x <= 2; x++) {
            for(int z = -2; z <= 2; z++) {
                if(x == 0 || z == 0) {
                    continue;
                }

                BlockPos thisPos = ceilingCenter.add(x, 0, z);

                IBlockState glassState = Blocks.STAINED_GLASS.getStateFromMeta(meta);
                cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ(), glassState);
            }
        }
    }


    private void generateEmptyCube(int chunkX, int chunkZ, ChunkPrimer cp) {
        List<BlockPos> cubePositions = StructureTools.getCubePositions(new BlockPos(15,ROOM_FLOOR_HEIGHT,15), ROOM_DIMENSION, ROOM_DIMENSION, ROOM_DIMENSION, true);
        for(BlockPos pos : cubePositions) {
            cp.setBlockState(pos.getX(), pos.getY(), pos.getZ(), WALL_BLOCK);
        }
    }

    private void generateConnections(int chunkX, int chunkZ, ChunkPrimer cp) {
        int center = (int) Math.floor(ROOM_DIMENSION / 2.0f);
        int lowest = ROOM_FLOOR_HEIGHT - ROOM_DIMENSION + 2;

        // left == negative X
        if(hasLeftNeighbor(chunkX)) {
            BlockPos lowerCenter = new BlockPos(16-ROOM_DIMENSION, lowest, 15-center);

            for(int z = -1; z < 2; z++) {
                for(int y = 0; y < 3; y++) {
                    BlockPos thisPos = lowerCenter.add(0, y, z);
                    cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ(), Blocks.AIR.getDefaultState());
                }
            }

            // And the bridge
            lowerCenter = lowerCenter.down();
            for(int offset = 1; offset < ROOM_PADDING+1; offset++) {
                // Floor + Ceiling
                for(int z = -1; z < 2; z++) {
                    BlockPos thisPos = lowerCenter.add(-offset, 0, z);

                    // Floor
                    cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ(), WALL_BLOCK);

                    // Ceiling
                    cp.setBlockState(thisPos.getX(), thisPos.getY()+4, thisPos.getZ(), WALL_BLOCK);
                }

                // Walls
                for(int yOffset = 0; yOffset < 3; yOffset++) {
                    BlockPos thisPos = lowerCenter.add(-offset, yOffset+1, 0);

                    // Left
                    cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ() + 2, WALL_BLOCK);

                    // Right
                    cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ() - 2, WALL_BLOCK);
                }
            }

        }

        // right == positive X
        if(hasRightNeighbor(chunkX)) {
            BlockPos lowerCenter = new BlockPos(15, lowest, 15-center);

            for(int z = -1; z < 2; z++) {
                for(int y = 0; y < 3; y++) {
                    BlockPos thisPos = lowerCenter.add(0, y, z);
                    cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ(), Blocks.AIR.getDefaultState());
                }
            }
        }

        // top == negative Z
        if(hasTopNeighbor(chunkZ)) {
            BlockPos lowerCenter = new BlockPos(15-center, lowest, 16-ROOM_DIMENSION);

            for(int x = -1; x < 2; x++) {
                for(int y = 0; y < 3; y++) {
                    BlockPos thisPos = lowerCenter.add(x, y, 0);
                    cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ(), Blocks.AIR.getDefaultState());
                }
            }

            // And the bridge
            lowerCenter = lowerCenter.down();
            for(int offset = 1; offset < ROOM_PADDING+1; offset++) {
                // Floor + Ceiling
                for(int x = -1; x < 2; x++) {
                    BlockPos thisPos = lowerCenter.add(x, 0, -offset);

                    // Floor
                    cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ(), WALL_BLOCK);

                    // Ceiling
                    cp.setBlockState(thisPos.getX(), thisPos.getY()+4, thisPos.getZ(), WALL_BLOCK);
                }

                // Walls
                for(int yOffset = 0; yOffset < 3; yOffset++) {
                    BlockPos thisPos = lowerCenter.add(0, yOffset+1, -offset);

                    // Left
                    cp.setBlockState(thisPos.getX() + 2, thisPos.getY(), thisPos.getZ(), WALL_BLOCK);

                    // Right
                    cp.setBlockState(thisPos.getX() - 2, thisPos.getY(), thisPos.getZ(), WALL_BLOCK);
                }
            }
        }

        // top == negative Z
        if(hasBottomNeighbor(chunkZ)) {
            BlockPos lowerCenter = new BlockPos(15-center, lowest, 15);

            for(int x = -1; x < 2; x++) {
                for(int y = 0; y < 3; y++) {
                    BlockPos thisPos = lowerCenter.add(x, y, 0);
                    cp.setBlockState(thisPos.getX(), thisPos.getY(), thisPos.getZ(), Blocks.AIR.getDefaultState());
                }
            }
        }

    }

    private boolean isOutside(int offsetChunkX, int offsetChunkZ) {
        if(offsetChunkX < 0 || offsetChunkX >= COLS) {
            return true;
        }

        if(offsetChunkZ < 0 || offsetChunkZ >= ROWS) {
            return true;
        }

        return false;
    }

    private boolean hasLeftNeighbor(int offsetChunkX) {
        return offsetChunkX > 0;
    }

    private boolean hasRightNeighbor(int offsetChunkX) {
        return offsetChunkX < COLS-1;
    }

    private boolean hasTopNeighbor(int offsetChunkZ) {
        return offsetChunkZ > 0;
    }

    private boolean hasBottomNeighbor(int offsetChunkZ) {
        return offsetChunkZ < ROWS-1;
    }


}
