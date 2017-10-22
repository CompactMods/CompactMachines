package org.dave.compactmachines3.world;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.world.tools.StructureTools;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;


public class WorldGenMachines implements IWorldGenerator {
    private static final Set<Block> allowedBlocksToSpawnOn;

    static {
        allowedBlocksToSpawnOn = new HashSet<>();
        allowedBlocksToSpawnOn.add(Blocks.SAND);
        allowedBlocksToSpawnOn.add(Blocks.SANDSTONE);
        allowedBlocksToSpawnOn.add(Blocks.DIRT);
        allowedBlocksToSpawnOn.add(Blocks.GRASS);
        allowedBlocksToSpawnOn.add(Blocks.STONE);
        allowedBlocksToSpawnOn.add(Blocks.COBBLESTONE);
        allowedBlocksToSpawnOn.add(Blocks.NETHERRACK);
        allowedBlocksToSpawnOn.add(Blocks.SOUL_SAND);
        allowedBlocksToSpawnOn.add(Blocks.END_STONE);
    }

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if(ConfigurationHandler.Settings.chanceForBrokenCube == 0.0f) {
            return;
        }

        boolean allowed = false;
        for(int dim : ConfigurationHandler.Settings.worldgenDimensions) {
            if(dim == world.provider.getDimension()) {
                allowed = true;
                break;
            }
        }

        if(!allowed) {
            return;
        }

        if(random.nextFloat() > ConfigurationHandler.Settings.chanceForBrokenCube) {
            return;
        }

        // Choose any random machine size but the biggest three
        EnumMachineSize size = EnumMachineSize.getFromMeta(random.nextInt(EnumMachineSize.values().length - 3));
        int dim = size.getDimension();
        int x = (chunkX << 4) + random.nextInt(16-dim) + dim;
        int z = (chunkZ << 4) + random.nextInt(16-dim) + dim;
        int y = world.getHeight(x, z) + dim;

        // Nether hack fix thingy because world.getHeight() is not useful there
        if(world.provider.getDimensionType() == DimensionType.NETHER) {
            y = 120;
            while(y > 0 && !world.isAirBlock(new BlockPos(x, y, z))) {
                y--;
            }
            while(y > 0 && world.isAirBlock(new BlockPos(x, y, z))) {
                y--;
            }
            if(y == 0) {
                return;
            }

            y+=dim;
        }

        BlockPos startingCornerPos = new BlockPos(x, y, z);

        // Do not generate on OCEAN type biomes
        Biome worldBiome = world.getBiome(startingCornerPos);
        if(worldBiome == Biomes.OCEAN || worldBiome == Biomes.DEEP_OCEAN || worldBiome == Biomes.FROZEN_OCEAN) {
            return;
        }


        // Only generate on the allowed blocks
        BlockPos foundationPos = startingCornerPos.offset(EnumFacing.DOWN);
        Block foundationBlock;
        do {
            foundationBlock = world.getBlockState(foundationPos).getBlock();
            if(foundationPos.getY() <= 0) {
                break;
            }

            if(!world.isAirBlock(foundationPos) && !(foundationBlock.isFoliage(world, foundationPos) || foundationBlock.isReplaceable(world, foundationPos))) {
                break;
            }

            foundationPos = foundationPos.offset(EnumFacing.DOWN);
        } while(true);

        if(!allowedBlocksToSpawnOn.contains(foundationBlock)) {
            return;
        }

        Logz.debug("Generating cube in dim=%d: chunkX=%d, chunkZ=%d, pos=%s", world.provider.getDimension(), chunkX, chunkZ, startingCornerPos);

        // Select one of the four top corners
        int cxSphere = x - (random.nextBoolean() ? dim : 0);
        int cySphere = y;
        int czSphere = z - (random.nextBoolean() ? dim : 0);
        int rSphere  = dim - random.nextInt(3);

        IBlockState state = Blockss.wallBreakable.getDefaultState();
        IBlockState fluidState = Blocks.LAVA.getDefaultState();
        for(BlockPos pos : StructureTools.getCubePositions(new BlockPos(x, y, z), dim+1, dim+1, dim+1, true)) {
            // Cut out a sphere at the selected corner
            float xx = pos.getX() - cxSphere;
            float yy = pos.getY() - cySphere;
            float zz = pos.getZ() - czSphere;

            if(Math.pow(xx, 2) + Math.pow(yy, 2) + Math.pow(zz, 2) < Math.pow(rSphere, 2)) {
                continue;
            }

            // Replace a few blocks with lava
            if(random.nextInt(100) <= 1) {
                world.setBlockState(pos, fluidState);
            } else {
                world.setBlockState(pos, state);
            }
        }
    }
}
