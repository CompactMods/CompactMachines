package dev.compactmods.machines.test;

import dev.compactmods.machines.api.Constants;
import net.minecraftforge.event.RegisterGameTestsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GameTestEventHandler {

    @SubscribeEvent
    public static void registerCrossmodGametests(final RegisterGameTestsEvent gametests) {
        final var mods = ModList.get();

        // if(mods.isLoaded("mekanism")) gametests.register(Mekanism.class);
    }
}
