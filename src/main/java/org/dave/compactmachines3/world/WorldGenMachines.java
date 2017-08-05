package org.dave.compactmachines3.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkGenerator;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.fml.common.IWorldGenerator;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.reference.EnumMachineSize;
import org.dave.compactmachines3.utility.Logz;
import org.dave.compactmachines3.world.tools.StructureTools;

import java.util.Random;


public class WorldGenMachines implements IWorldGenerator {

    @Override
    public void generate(Random random, int chunkX, int chunkZ, World world, IChunkGenerator chunkGenerator, IChunkProvider chunkProvider) {
        if(world.provider.getDimension() != 0) {
            return;
        }

        if(ConfigurationHandler.Settings.chanceForBrokenCube == 0.0f) {
            return;
        }

        if(random.nextFloat() > ConfigurationHandler.Settings.chanceForBrokenCube) {
            return;
        }

        Logz.debug("Generating cube in overworld: chunkX=%d, chunkZ=%d", chunkX, chunkZ);

        // Choose any random machine size but the biggest three
        EnumMachineSize size = EnumMachineSize.getFromMeta(random.nextInt(EnumMachineSize.values().length - 3));
        int dim = size.getDimension();
        int x = (chunkX << 4) + random.nextInt(16-dim) + dim;
        int z = (chunkZ << 4) + random.nextInt(16-dim) + dim;
        int y = world.getHeight(x, z) + dim;

        // Do not generate on OCEAN type biomes
        Biome worldBiome = world.getBiome(new BlockPos(x, y, z));
        if(worldBiome == Biomes.OCEAN || worldBiome == Biomes.DEEP_OCEAN || worldBiome == Biomes.FROZEN_OCEAN) {
            return;
        }

        // Select one of the four top corners
        int cxSphere = x - (random.nextBoolean() ? dim : 0);
        int cySphere = y;
        int czSphere = z - (random.nextBoolean() ? dim : 0);
        int rSphere  = dim - random.nextInt(3);

        IBlockState state = Blockss.wallBreakable.getDefaultState();
        IBlockState fluidState = Blockss.miniaturizationFluidBlock.getDefaultState();
        for(BlockPos pos : StructureTools.getCubePositions(new BlockPos(x, y, z), dim+1, dim+1, dim+1, true)) {
            // Cut out a sphere at the selected corner
            float xx = pos.getX() - cxSphere;
            float yy = pos.getY() - cySphere;
            float zz = pos.getZ() - czSphere;

            if(Math.pow(xx, 2) + Math.pow(yy, 2) + Math.pow(zz, 2) < Math.pow(rSphere, 2)) {
                continue;
            }

            if(random.nextInt(100) <= 5) {
                world.setBlockState(pos, fluidState);
            } else {
                world.setBlockState(pos, state);
            }
        }
    }
}
