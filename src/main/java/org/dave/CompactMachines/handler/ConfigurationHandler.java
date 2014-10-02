package org.dave.CompactMachines.handler;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigurationHandler {
    public static Configuration configuration;
    public static boolean testValue = false;
    public static int dimensionId;
    public static int maxDroppedStacks;
    public static int chunkLoadingMode;
    public static int cubeDistance;
    public static int cooldownRF;
    public static int cooldownItems;
    public static int cooldownFluid;

    public static void init(File configFile) {
        // Create the configuration object from the given configuration file
        if (configuration == null) {
            configuration = new Configuration(configFile);
            loadConfiguration();
        }
    }

    private static void loadConfiguration() {
        dimensionId = configuration.getInt("dimension", "Internal", -1, Integer.MIN_VALUE, Integer.MAX_VALUE, "Dimension used for machines. Do not change this unless it is somehow conflicting!");
        cubeDistance = configuration.getInt("cubeDistance", "Internal", 64, 16, Integer.MAX_VALUE, "The distance between the cubes in the machine dimension! Must be a multiple of 16, i.e. 16, 32, 48, 64... DO NOT CHANGE THIS.");
        maxDroppedStacks = configuration.getInt("maxDroppedStacks", "CompactMachines", 128, 0, Integer.MAX_VALUE, "Maximum number of items dropping when breaking a Compact Machine");
        chunkLoadingMode = configuration.getInt("chunkLoadingMode", "CompactMachines", 1, 0, 2, "Chunk Loading Mode: 0 = Never, 1 = Always, 2 = When machine is loaded");

        cooldownRF = configuration.getInt("cooldownRF", "CompactMachines", 0, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Redstone Flux, i.e. 20 => 10000 RF/s, 0 => 10000 RF/t");
        cooldownItems = configuration.getInt("cooldownItems", "CompactMachines", 10, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Items, i.e. 40 => 1 Stack every two seconds");
        cooldownFluid = configuration.getInt("cooldownFluid", "CompactMachines", 10, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Fluids, i.e. 0 => 1 Bucket per tick");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static void saveConfiguration() {
    	Property dimProp = configuration.get("Internal", "dimension", -1, "Dimension used for machines. Do not change this unless it is somehow conflicting!");
    	dimProp.set(dimensionId);

    	configuration.save();
    }

    @SubscribeEvent
    public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equalsIgnoreCase(Reference.MOD_ID)) {
            loadConfiguration();
        }
    }
}
