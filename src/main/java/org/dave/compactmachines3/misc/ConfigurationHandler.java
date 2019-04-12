package org.dave.compactmachines3.misc;

import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.utility.Logz;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationHandler {
    public static Configuration configuration;
    public static File cmDirectory;

    private static final String CATEGORY_INTERNAL = "Internal";
    private static final String CATEGORY_MINIATURIZATION = "Miniaturization";
    private static final String CATEGORY_MACHINES = "Machines";
    private static final String CATEGORY_COMPAT = "Compatibility";

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

        recipeDirectory = new File(cmDirectory, "recipes");
        if(!recipeDirectory.exists()) {
            recipeDirectory.mkdir();
        }

        schemaDirectory = new File(cmDirectory, "schemas");
        if(!schemaDirectory.exists()) {
            schemaDirectory.mkdir();
        }

    }

    private static void loadConfiguration() {
        Logz.info("Loading configuration");
        Settings.dimensionId = configuration.getInt(
                "dimensionId",
                CATEGORY_INTERNAL,
                144,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                "Dimension used for machines. Do not change this unless it is somehow conflicting!",
                "Dimension ID"
        );

        Settings.dimensionTypeId = configuration.getInt(
                "dimensionTypeId",
                CATEGORY_INTERNAL,
                144,
                Integer.MIN_VALUE, Integer.MAX_VALUE,
                "Dimension type used for machines. Do not change this unless it is somehow conflicting!",
                "Dimension Type ID"
        );

        Settings.forceLoadChunks = configuration.getBoolean(
                "forceLoadChunks",
                CATEGORY_INTERNAL,
                false,
                "Whether the interior of all Compact Machines should be chunk loaded always. Otherwise they will only chunkload when the CM itself is chunkloaded.",
                "Force chunk load"
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
                "allowRespawning",
                CATEGORY_MACHINES,
                true,
                "Whether players can respawn inside of Compact Machines, i.e. place beds and sleep there",
                "Allow Respawning"
        );

        MachineSettings.keepPlayersInside = configuration.getBoolean(
                "keepPlayersInside",
                CATEGORY_MACHINES,
                true,
                "Block players from leaving the Compact Machine they should currently be in",
                "Keep players inside of Machines"
        );

        MachineSettings.spawnRate = configuration.getInt(
                "spawnRate",
                CATEGORY_MACHINES,
                1200,
                1,
                Integer.MAX_VALUE,
                "How often to try spawning entities inside of machines in ticks",
                "Spawn Rate"
        );

        MachineSettings.allowPeacefulSpawns = configuration.getBoolean(
                "allowPeacefulSpawns",
                CATEGORY_MACHINES,
                true,
                "Allow peaceful creatures to spawn inside of machines",
                "Allow Peaceful Spawns"
        );

        MachineSettings.allowHostileSpawns = configuration.getBoolean(
                "allowHostileSpawns",
                CATEGORY_MACHINES,
                true,
                "Allow hostile creatures to spawn inside of machines",
                "Allow Hostile Spawns"
        );

        MachineSettings.allowEnteringWithoutPSD = configuration.getBoolean(
                "allowEnteringWithoutPSD",
                CATEGORY_MACHINES,
                true,
                "Allow players to enter machines with other means than the PSD",
                "Allow entering without machines"
        );

        MachineSettings.renderTileEntitiesInGUI = configuration.getBoolean(
                "renderTileEntitiesInGUI",
                CATEGORY_MACHINES,
                true,
                "Client-side option, might give a performance boost when opening a Machine GUI",
                "Render TileEntities in GUI"
        );

        MachineSettings.renderLivingEntitiesInGUI = configuration.getBoolean(
                "renderLivingEntitiesInGUI",
                CATEGORY_MACHINES,
                true,
                "Client-side option, might give a performance boost when opening a Machine GUI",
                "Render living entities in GUI"
        );

        MachineSettings.autoUpdateRate = configuration.getInt(
                "autoUpdateRate",
                CATEGORY_MACHINES,
                20,
                0, Integer.MAX_VALUE,
                "Update the machine view GUI every n ticks. Set to 0 to disable. Lower values decrease performance significantly!"
        );

        CompatSettings.doesWaterVaporize = configuration.getBoolean(
                "doesWaterVaporize",
                CATEGORY_COMPAT,
                false,
                "Forces water to vaporize inside Compact Machines. Used for Forever Stranded: Lost Souls.",
                "Force water to vaporize"
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
    public static void onConfigurationChanged(ConfigChangedEvent event) {
        if(!event.getModID().equalsIgnoreCase(CompactMachines3.MODID)) {
            return;
        }

        loadConfiguration();
    }

    public static List<IConfigElement> getConfigElements() {
        List<IConfigElement> result = new ArrayList<>();
        result.add(new ConfigElement(configuration.getCategory(CATEGORY_MINIATURIZATION)));
        result.add(new ConfigElement(configuration.getCategory(CATEGORY_MACHINES)));
        result.add(new ConfigElement(configuration.getCategory(CATEGORY_COMPAT)));
        result.add(new ConfigElement(configuration.getCategory(CATEGORY_INTERNAL)));

        return result;
    }

    public static class CompatSettings {
        public static boolean doesWaterVaporize;
    }

    public static class MachineSettings {
        public static boolean allowRespawning;
        public static boolean keepPlayersInside;
        public static int spawnRate;
        public static boolean allowPeacefulSpawns;
        public static boolean allowHostileSpawns;
        public static boolean allowEnteringWithoutPSD;
        public static boolean renderTileEntitiesInGUI;
        public static boolean renderLivingEntitiesInGUI;
        public static int autoUpdateRate;
    }

    public static class Settings {
        public static int dimensionId;
        public static int dimensionTypeId;
        public static boolean forceLoadChunks;
        public static int maximumCraftingAreaSize;
        public static int maximumCraftingCatalystAge;

        public static int getMaximumMagnitude() {
            return ConfigurationHandler.Settings.maximumCraftingAreaSize-1 / 2;
        }
    }
}
