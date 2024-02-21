package dev.compactmods.machines.neoforge.compat;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.neoforge.compat.carryon.CarryOnCompat;
import dev.compactmods.machines.neoforge.compat.curios.CuriosCompat;
import dev.compactmods.machines.neoforge.compat.theoneprobe.TheOneProbeCompat;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InterModCompat {

    @SubscribeEvent
    public static void enqueueCompatMessages(final InterModEnqueueEvent evt) {
        if(ModList.get().isLoaded("theoneprobe"))
            TheOneProbeCompat.sendIMC();

        if(ModList.get().isLoaded("carryon"))
            CarryOnCompat.sendIMC();

        if(ModList.get().isLoaded("curios"))
            CuriosCompat.sendIMC();
    }
}
