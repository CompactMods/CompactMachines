package dev.compactmods.machines.client.command;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public class CMClientCommands {
    public static void registerClientCommands(RegisterClientCommandsEvent evt) {
        final var dispatcher = evt.getDispatcher();

        if (!FMLEnvironment.isProduction()) {
            ScreenSizesCommand.registerScreenSizesCommands(dispatcher);
        }
    }
}
