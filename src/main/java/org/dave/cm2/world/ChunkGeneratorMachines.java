package org.dave.cm2.world;

import com.google.common.collect.ImmutableList;
import mcjty.lib.compat.CompatChunkGenerator;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.List;

public class ChunkGeneratorMachines implements CompatChunkGenerator {
    private final World world;

    public ChunkGeneratorMachines(World worldIn) {
        this.world = worldIn;
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        ChunkPrimer cp = new ChunkPrimer();
        Chunk chunk = new Chunk(this.world, cp, x, z);
        chunk.generateSkylightMap();
        return chunk;
    }

    @Override
    public void populate(int x, int z) {
    }

    @Override
    public boolean generateStructures(Chunk chunkIn, int x, int z) {
        return false;
    }

    @Override
    public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
        return ImmutableList.of();
    }

    @Override
    public void recreateStructures(Chunk chunkIn, int x, int z) {
    }

    @Override
    public BlockPos clGetStrongholdGen(World worldIn, String structureName, BlockPos position) {
        return null;
    }
}
