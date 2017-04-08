package org.dave.cm2.misc;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.utility.Logz;

import java.io.File;

public class ConfigurationHandler {
    public static Configuration configuration;

    private static final String CATEGORY_INTERNAL = "Internal";
    private static final String CATEGORY_MINIATURIZATION = "Miniaturization";
    private static final String CATEGORY_MACHINES = "Machines";

    public static void init(File configFile) {
        if(configuration != null) {
            return;
        }

        configuration = new Configuration(configFile, null);
        loadConfiguration();
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

        PotionSettings.onEatAmplifier = configuration.getInt(
                PotionSettings.ON_EAT_AMPLIFIER_NAME,
                CATEGORY_MINIATURIZATION,
                PotionSettings.ON_EAT_AMPLIFIER_DEFAULT,
                0, 3,
                PotionSettings.ON_EAT_AMPLIFIER_COMMENT,
                PotionSettings.ON_EAT_AMPLIFIER_LABEL
        );

        PotionSettings.onEatDuration = configuration.getInt(
                PotionSettings.ON_EAT_DURATION_NAME,
                CATEGORY_MINIATURIZATION,
                PotionSettings.ON_EAT_DURATION_DEFAULT,
                0, 12000,
                PotionSettings.ON_EAT_DURATION_COMMENT,
                PotionSettings.ON_EAT_DURATION_LABEL
        );

        PotionSettings.onBlockContactAmplifier = configuration.getInt(
                PotionSettings.OBC_AMPLIFIER_NAME,
                CATEGORY_MINIATURIZATION,
                PotionSettings.OBC_AMPLIFIER_DEFAULT,
                0, 3,
                PotionSettings.OBC_AMPLIFIER_COMMENT,
                PotionSettings.OBC_AMPLIFIER_LABEL
        );

        PotionSettings.onBlockContactDuration = configuration.getInt(
                PotionSettings.OBC_DURATION_NAME,
                CATEGORY_MINIATURIZATION,
                PotionSettings.OBC_DURATION_DEFAULT,
                0, 12000,
                PotionSettings.OBC_DURATION_COMMENT,
                PotionSettings.OBC_DURATION_LABEL
        );

        MachineSettings.allowRespawning = configuration.getBoolean(
                MachineSettings.ALLOW_RESPAWN_NAME,
                CATEGORY_MACHINES,
                MachineSettings.ALLOW_RESPAWN_DEFAULT,
                MachineSettings.ALLOW_RESPAWN_COMMENT,
                MachineSettings.ALLOW_RESPAWN_LABEL
        );

        MachineSettings.fluidCostForEntering = configuration.getInt(
                MachineSettings.FLUID_COST_NAME,
                CATEGORY_MACHINES,
                MachineSettings.FLUID_COST_DEFAULT,
                0, 4000,
                MachineSettings.FLUID_COST_COMMENT,
                MachineSettings.FLUID_COST_LABEL
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
        if(!event.getModID().equalsIgnoreCase(CompactMachines2.MODID)) {
            return;
        }

        loadConfiguration();
    }

    public static class MachineSettings {
        public static boolean allowRespawning;
        public static int fluidCostForEntering;

        private static final String ALLOW_RESPAWN_NAME = "allowRespawning";
        private static final boolean ALLOW_RESPAWN_DEFAULT = true;
        private static final String ALLOW_RESPAWN_COMMENT = "Whether players can respawn inside of Compact Machines, i.e. place beds and sleep there";
        private static final String ALLOW_RESPAWN_LABEL = "Allow Respawning";

        private static final String FLUID_COST_NAME = "fluidCostForEntering";
        private static final int FLUID_COST_DEFAULT = 250;
        private static final String FLUID_COST_COMMENT = "How much miniaturization fluid needs to be in the PSD to be able to enter a machine in mB";
        private static final String FLUID_COST_LABEL = "Required fluid to enter machine";
    }

    public static class PotionSettings {
        public static int onBlockContactDuration;
        public static int onBlockContactAmplifier;
        public static int onEatDuration;
        public static int onEatAmplifier;

        private static final String ON_EAT_DURATION_NAME = "onEatDuration";
        private static final int ON_EAT_DURATION_DEFAULT = 400;
        private static final String ON_EAT_DURATION_COMMENT = "How long the 'Shrunk' effect is applied to players, when they eat a Miniaturization Fluid Drop.";
        private static final String ON_EAT_DURATION_LABEL = "Eating fluid drops - Effect duration";

        private static final String ON_EAT_AMPLIFIER_NAME = "onEatAmplifier";
        private static final int ON_EAT_AMPLIFIER_DEFAULT = 3;
        private static final String ON_EAT_AMPLIFIER_COMMENT = "How strong the 'Shrunk' effect is when applied to entities eating Miniaturization Fluid Drops";
        private static final String ON_EAT_AMPLIFIER_LABEL = "Eating fluid drops - Effect Amplifier";

        private static final String OBC_DURATION_NAME = "onBlockContactDuration";
        private static final int OBC_DURATION_DEFAULT = 200;
        private static final String OBC_DURATION_COMMENT = "How long the 'Shrunk' effect is applied to entities when they touch miniaturization fluid. Set to 0 to disable.";
        private static final String OBC_DURATION_LABEL = "Fluid contact - Effect duration";

        private static final String OBC_AMPLIFIER_NAME = "onBlockContactAmplifier";
        private static final int OBC_AMPLIFIER_DEFAULT = 1;
        private static final String OBC_AMPLIFIER_COMMENT = "How strong the 'Shrunk' effect is when applied to entities touching miniaturization fluid";
        private static final String OBC_AMPLIFIER_LABEL = "Fluid contact - Effect Amplifier";
    }

    public static class Settings {
        public static int dimensionId;
        public static int dimensionTypeId;
        public static boolean forceLoadChunks;
        public static float chanceForBrokenCube;

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
