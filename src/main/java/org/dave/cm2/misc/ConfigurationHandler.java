package org.dave.cm2.misc;

import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.dave.cm2.CompactMachines2;
import org.dave.cm2.utility.Logz;

import java.io.File;

public class ConfigurationHandler {
    public static Configuration configuration;

    private static final String CATEGORY_INTERNAL = "Internal";

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

    public static class Settings {
        public static int dimensionId;
        public static int dimensionTypeId;
        public static boolean forceLoadChunks;

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
    }
}
