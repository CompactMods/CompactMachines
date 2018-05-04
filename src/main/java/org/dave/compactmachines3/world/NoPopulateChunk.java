package org.dave.compactmachines3.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class NoPopulateChunk extends Chunk {
    private static Field isTerrainPopulatedField;
    private static Field isLightPopulatedField;

    public static void initReflection() {
        isTerrainPopulatedField = ReflectionHelper.findField(Chunk.class, "field_76646_k", "isTerrainPopulated");
        isTerrainPopulatedField.setAccessible(true);

        isLightPopulatedField = ReflectionHelper.findField(Chunk.class, "field_150814_l", "isLightPopulated");
        isLightPopulatedField.setAccessible(true);
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
