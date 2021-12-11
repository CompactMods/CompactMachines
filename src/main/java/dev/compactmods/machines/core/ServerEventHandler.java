package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.command.CMCommandRoot;
import dev.compactmods.machines.rooms.chunkloading.CMRoomChunkloadingManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerStarting(final ServerStartingEvent evt) {
        MinecraftServer server = evt.getServer();

        CompactMachines.CHUNKLOAD_MANAGER = new CMRoomChunkloadingManager(server);
        // SavedMachineDataMigrator.migrate(server);
    }

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        CMCommandRoot.register(event.getDispatcher());
    }
}
