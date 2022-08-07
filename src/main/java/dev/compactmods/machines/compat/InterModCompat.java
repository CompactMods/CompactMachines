package dev.compactmods.machines.compat;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.compat.carryon.CarryOnCompat;
import dev.compactmods.machines.compat.theoneprobe.TheOneProbeCompat;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InterModCompat {

    @SubscribeEvent
    public static void enqueueCompatMessages(final InterModEnqueueEvent evt) {
        if(ModList.get().isLoaded("theoneprobe"))
            TheOneProbeCompat.sendIMC();

        if(ModList.get().isLoaded("carryon"))
            CarryOnCompat.sendIMC();
    }
}
