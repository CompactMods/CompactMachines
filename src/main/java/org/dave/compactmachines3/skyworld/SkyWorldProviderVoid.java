package org.dave.compactmachines3.skyworld;

import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.lex.yunomakegoodmap.WorldProviderEndVoid;

public class SkyWorldProviderVoid extends WorldProviderEnd {
    @Override
    public IChunkGenerator createChunkGenerator() {
        if(world.getWorldType() == SkyDimension.worldType) {
            String genOptions = world.getWorldInfo().getGeneratorOptions();
            boolean useVoidWorlds = new SkyWorldConfiguration(genOptions).voidDimensions;

            if(useVoidWorlds) {
                return new WorldProviderEndVoid.ChunkGeneratorEndVoid(world, world.getSeed(), this.getSpawnPoint());
            }
        }

        return super.createChunkGenerator();
    }
}
