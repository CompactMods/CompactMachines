package org.dave.compactmachines3.skyworld;

import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SkyChunkGenerator implements IChunkGenerator {
    private final World world;
    private final SkyTerrainGenerator terrainGen;
    public final SkyWorldConfiguration config;

    public Random random;

    public SkyChunkGenerator(World world, String generatorOptions) {
        this.world = world;
        this.random = new Random(world.getSeed());
        this.config = new SkyWorldConfiguration(generatorOptions);

        this.terrainGen = new SkyTerrainGenerator(world, this);
    }

    @Override
    public Chunk generateChunk(int chunkX, int chunkZ) {
        ChunkPrimer cp = new ChunkPrimer();

        this.terrainGen.generate(chunkX, chunkZ, cp);

        Chunk chunk = new Chunk(this.world, cp, chunkX, chunkZ);
        chunk.generateSkylightMap();

        // TODO: Set biome
        // this.terrainGen.setBiomes(chunkX, chunkZ, chunk);

        return chunk;
    }

    @Override
    public void populate(int chunkX, int chunkZ) {
        this.terrainGen.populate(chunkX, chunkZ);
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return ImmutableList.of();
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return null;
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {

    }

    @Override
    public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos) {
        return false;
    }
}
