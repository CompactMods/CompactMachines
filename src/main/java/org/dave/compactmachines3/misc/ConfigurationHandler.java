package org.dave.compactmachines3.misc;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.utility.JarExtract;
import org.dave.compactmachines3.utility.Logz;

import java.io.*;

public class ConfigurationHandler {
    public static Configuration configuration;
    public static File cmDirectory;

    private static final String CATEGORY_INTERNAL = "Internal";
    private static final String CATEGORY_MINIATURIZATION = "Miniaturization";
    private static final String CATEGORY_MACHINES = "Machines";

    public static File schemaDirectory;
    public static File recipeDirectory;

    public static void init(File configFile) {
        if(configuration != null) {
            return;
        }

        cmDirectory = new File(configFile.getParentFile(), "compactmachines3");
        if(!cmDirectory.exists()) {
            cmDirectory.mkdir();
        }

        configuration = new Configuration(new File(cmDirectory, "settings.cfg"), null);
        loadConfiguration();

        // Extract recipes if the folder does not exist
        recipeDirectory = new File(cmDirectory, "recipes");
        if(!recipeDirectory.exists()) {
            recipeDirectory.mkdir();
            int count = JarExtract.copy("assets/compactmachines3/config/recipes", recipeDirectory);
            Logz.info("Extracted %d recipes to config folder", count);
        }

        schemaDirectory = new File(cmDirectory, "schemas");
        if(!schemaDirectory.exists()) {
            schemaDirectory.mkdir();
        }

    }

    private static void loadConfiguration() {
        Logz.info("Loading configuration");
        Settings.dimensionId = configuration.getInt(
                Settings.DIMENSION_ID_NAME,
                CATEGORY_INTERNAL,
                Settings.DIMENSION_ID_DEFAULT,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Settings.DIMENSION_ID_COMMENT,
                Settings.DIMENSION_ID_LABEL
        );

        Settings.dimensionTypeId = configuration.getInt(
                Settings.DIMENSION_TYPE_ID_NAME,
                CATEGORY_INTERNAL,
                Settings.DIMENSION_TYPE_ID_DEFAULT,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                Settings.DIMENSION_TYPE_ID_COMMENT,
                Settings.DIMENSION_TYPE_ID_LABEL
        );

        Settings.forceLoadChunks = configuration.getBoolean(
                Settings.FORCELOADCHUNKS_NAME,
                CATEGORY_INTERNAL,
                Settings.FORCELOADCHUNKS_DEFAULT,
                Settings.FORCELOADCHUNKS_COMMENT,
                Settings.FORCELOADCHUNKS_LABEL
        );

        Settings.chanceForBrokenCube = configuration.getFloat(
                Settings.WORLDGEN_CHANCE_NAME,
                CATEGORY_INTERNAL,
                Settings.WORLDGEN_CHANCE_DEFAULT,
                0.0f, 1.0f,
                Settings.WORLDGEN_CHANCE_COMMENT,
                Settings.WORLDGEN_CHANCE_LABEL
        );

        Settings.maximumCraftingAreaSize = configuration.getInt(
                "maximumCraftingAreaSize",
                CATEGORY_MINIATURIZATION,
                15,
                5, 20,
                "Maximum size the field projectors can cover",
                "Maximum Crafting Area Size"
        );

        Settings.maximumCraftingCatalystAge = configuration.getInt(
                "maximumCraftingCatalystAge",
                CATEGORY_MINIATURIZATION,
                60,
                20, Integer.MAX_VALUE,
                "Maximum age in ticks in which an item is valid for acting as a catalyst",
                "Maximum Crafting Catalyst Age"
        );

        MachineSettings.allowRespawning = configuration.getBoolean(
                MachineSettings.ALLOW_RESPAWN_NAME,
                CATEGORY_MACHINES,
                MachineSettings.ALLOW_RESPAWN_DEFAULT,
                MachineSettings.ALLOW_RESPAWN_COMMENT,
                MachineSettings.ALLOW_RESPAWN_LABEL
        );

        if(configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static void saveConfiguration() {
        Logz.info("Saving configuration");
        configuration.save();
    }

    @SubscribeEvent
    public void onConfigurationChanged(ConfigChangedEvent event) {
        if(!event.getModID().equalsIgnoreCase(CompactMachines3.MODID)) {
            return;
        }

        loadConfiguration();
    }

    public static class MachineSettings {
        public static boolean allowRespawning;

        private static final String ALLOW_RESPAWN_NAME = "allowRespawning";
        private static final boolean ALLOW_RESPAWN_DEFAULT = true;
        private static final String ALLOW_RESPAWN_COMMENT = "Whether players can respawn inside of Compact Machines, i.e. place beds and sleep there";
        private static final String ALLOW_RESPAWN_LABEL = "Allow Respawning";
    }

    public static class Settings {
        public static int dimensionId;
        public static int dimensionTypeId;
        public static boolean forceLoadChunks;
        public static float chanceForBrokenCube;
        public static int maximumCraftingAreaSize;
        public static int maximumCraftingCatalystAge;

        public static int getMaximumMagnitude() {
            return ConfigurationHandler.Settings.maximumCraftingAreaSize-1 / 2;
        }

        private static final String DIMENSION_ID_NAME = "dimensionId";
        private static final int DIMENSION_ID_DEFAULT = 144;
        private static final String DIMENSION_ID_COMMENT = "Dimension used for machines. Do not change this unless it is somehow conflicting!";
        private static final String DIMENSION_ID_LABEL = "Dimension ID";

        private static final String DIMENSION_TYPE_ID_NAME = "dimensionTypeId";
        private static final int DIMENSION_TYPE_ID_DEFAULT = 144;
        private static final String DIMENSION_TYPE_ID_COMMENT = "Dimension type used for machines. Do not change this unless it is somehow conflicting!";
        private static final String DIMENSION_TYPE_ID_LABEL = "Dimension Type ID";

        private static final String FORCELOADCHUNKS_NAME = "forceLoadChunks";
        private static final boolean FORCELOADCHUNKS_DEFAULT = false;
        private static final String FORCELOADCHUNKS_COMMENT = "Whether the interior of all Compact Machines should be chunk loaded always. Otherwise they will only chunkload when the CM itself is chunkloaded.";
        private static final String FORCELOADCHUNKS_LABEL = "Force chunk load";

        private static final String WORLDGEN_CHANCE_NAME = "worldgenChance";
        private static final float WORLDGEN_CHANCE_DEFAULT = 0.0005f;
        private static final String WORLDGEN_CHANCE_COMMENT = "The chance a chunk in the overworld contains a broken compact machine";
        private static final String WORLDGEN_CHANCE_LABEL = "Worldgen Chance";


    }
}
