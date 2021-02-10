package org.dave.compactmachines3.world.tools;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.misc.ConfigurationHandler;
import org.dave.compactmachines3.world.WorldProviderMachines;

import javax.annotation.Nullable;

public class DimensionTools {
    public static DimensionType baseType;

    public static void registerDimension() {
        CompactMachines3.logger.info("Registering dimension type: {}", ConfigurationHandler.Settings.dimensionTypeId);
        DimensionTools.baseType = DimensionType.register("CompactMachines", "_suffix", ConfigurationHandler.Settings.dimensionTypeId, WorldProviderMachines.class, false);

        CompactMachines3.logger.info("Registering Compact Machines 3 dimension: {}", ConfigurationHandler.Settings.dimensionId);
        DimensionManager.registerDimension(ConfigurationHandler.Settings.dimensionId, DimensionTools.baseType);
    }

    @Nullable
    public static WorldServer getWorldServerForDimension(int dim) {
        return FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(dim);
    }

    @Nullable
    public static WorldServer getServerMachineWorld() {
        return getWorldServerForDimension(ConfigurationHandler.Settings.dimensionId);
    }
}
