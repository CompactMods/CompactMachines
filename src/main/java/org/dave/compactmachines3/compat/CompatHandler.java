package org.dave.compactmachines3.compat;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import org.dave.compactmachines3.CompactMachines3;

public class CompatHandler {
    public static void registerCompat() {
        registerTheOneProbe();
        registerWaila();
    }

    private static void registerTheOneProbe() {
        if (Loader.isModLoaded("theoneprobe")) {
            CompactMachines3.logger.info("Trying to tell The One Probe about us");
            TopCompatibility.register();
        }
    }

    private static void registerWaila() {
        if (Loader.isModLoaded("waila")) {
            CompactMachines3.logger.info("Trying to tell Waila about us");
            FMLInterModComms.sendMessage("waila", "register", "org.dave.compactmachines3.compat.WailaProvider.register");
        }
    }
}
