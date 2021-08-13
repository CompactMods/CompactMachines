package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerStarting(final FMLServerStartingEvent evt) {
        MinecraftServer server = evt.getServer();
        // SavedMachineDataMigrator.migrate(server);
    }
}
