package dev.compactmods.machines.neoforge.compat;

import dev.compactmods.machines.api.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InterModCompat {

    @SubscribeEvent
    public static void enqueueCompatMessages(final InterModEnqueueEvent evt) {
//        if(ModList.get().isLoaded("theoneprobe"))
//            TheOneProbeCompat.sendIMC();
//
//        if(ModList.get().isLoaded("carryon"))
//            CarryOnCompat.sendIMC();
    }
}
