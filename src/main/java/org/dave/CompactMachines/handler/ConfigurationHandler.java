package org.dave.CompactMachines.handler;

import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class ConfigurationHandler {
    public static Configuration configuration;
    public static boolean testValue = false;
    public static int dimensionId;

    public static void init(File configFile) {
        // Create the configuration object from the given configuration file
        if (configuration == null) {
            configuration = new Configuration(configFile);
            loadConfiguration();
        }
    }

    private static void loadConfiguration() {
        dimensionId = configuration.getInt("dimension", "Internal", -1, Integer.MIN_VALUE, Integer.MAX_VALUE, "Dimension used for machines. Do not change this unless it is somehow conflicting!");

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
