package dev.compactmods.machines.test;

import dev.compactmods.machines.api.Constants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TestEntry {

    @SubscribeEvent
    public static void registerCrossmodGametests(final RegisterGameTestsEvent gametests) {
        final var mods = ModList.get();

        // add more
    }
}
