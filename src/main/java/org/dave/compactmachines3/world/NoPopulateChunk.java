package org.dave.compactmachines3.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import java.lang.reflect.Field;

public class NoPopulateChunk extends Chunk {
    private static Field isTerrainPopulatedField;
    private static Field isLightPopulatedField;

    public static void initReflection() {
        try {
            isTerrainPopulatedField = Chunk.class.getDeclaredField("isTerrainPopulated");
            isTerrainPopulatedField.setAccessible(true);

            isLightPopulatedField = Chunk.class.getDeclaredField("isLightPopulated");
            isLightPopulatedField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static void setChunkPopulatedFields(Chunk chunk) {
        try {
            isTerrainPopulatedField.set(chunk, true);
            isLightPopulatedField.set(chunk, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public NoPopulateChunk(World worldIn, ChunkPrimer primer, int x, int z) {
        super(worldIn, primer, x, z);
    }

    @Override
    protected void populate(IChunkGenerator generator) {
        setChunkPopulatedFields(this);
    }
}
