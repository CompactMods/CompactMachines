package dev.compactmods.machines.neoforge.compat.theoneprobe;

import net.neoforged.fml.InterModComms;

public class TheOneProbeCompat {
    public static void sendIMC() {
        InterModComms.sendTo("theoneprobe", "getTheOneProbe", TheOneProbeMain::new);
    }
}
