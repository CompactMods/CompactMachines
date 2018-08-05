package org.dave.compactmachines3.compat;

import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import org.dave.compactmachines3.skyworld.SkyWorldProviderNether;
import org.dave.compactmachines3.skyworld.SkyWorldProviderVoid;

public class YUNoMakeGoodMap {
    public static void init() {
        DimensionManager.unregisterDimension(-1);
        DimensionManager.unregisterDimension(1);

        DimensionManager.registerDimension(-1, DimensionType.register("Nether (CompactSky)", "_nether", 1, SkyWorldProviderNether.class, false));
        DimensionManager.registerDimension(1, DimensionType.register("The End (CompactSky)", "_end", 1, SkyWorldProviderVoid.class, false));
    }
}
