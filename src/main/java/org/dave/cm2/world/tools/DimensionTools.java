package org.dave.cm2.world.tools;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.dave.cm2.misc.ConfigurationHandler;
import org.dave.cm2.utility.Logz;
import org.dave.cm2.world.WorldProviderMachines;

public class DimensionTools {
    public static DimensionType baseType;

    public static void registerDimension() {
        Logz.info("Registering dimension type: " + ConfigurationHandler.Settings.dimensionTypeId);
        DimensionTools.baseType = DimensionType.register("CompactMachines", "_suffix", ConfigurationHandler.Settings.dimensionTypeId, WorldProviderMachines.class, false);

        Logz.info("Registering CM2 dimension: " + ConfigurationHandler.Settings.dimensionId);
        DimensionManager.registerDimension(ConfigurationHandler.Settings.dimensionId, DimensionTools.baseType);

    }

    public static WorldServer getWorldServerForDimension(int dim) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dim);
    }

    public static WorldServer getServerMachineWorld() {
        return getWorldServerForDimension(ConfigurationHandler.Settings.dimensionId);
    }
}
