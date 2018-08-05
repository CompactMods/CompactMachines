package org.dave.compactmachines3.skyworld;

import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.lex.yunomakegoodmap.WorldProviderHellVoid;
import net.minecraftforge.lex.yunomakegoodmap.YUNoMakeGoodMap;

public class SkyWorldProviderNether extends WorldProviderHell {
    @Override
    public IChunkGenerator createChunkGenerator() {
        if(world.getWorldType() == SkyDimension.worldType) {
            String genOptions = world.getWorldInfo().getGeneratorOptions();
            boolean useVoidWorlds = new SkyWorldConfiguration(genOptions).voidDimensions;

            if(useVoidWorlds) {
                return new WorldProviderHellVoid.ChunkGeneratorHellVoid(world, YUNoMakeGoodMap.instance.shouldGenerateNetherFortress(world), world.getSeed());
            }
        }

        return super.createChunkGenerator();
    }
}