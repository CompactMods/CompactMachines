package dev.compactmods.machines.core;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.command.CMCommandRoot;
import dev.compactmods.machines.command.CMDataCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CompactMachines.MOD_ID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onCommandsRegister(final RegisterCommandsEvent event) {
        final var dispatcher = event.getDispatcher();
        CMCommandRoot.register(dispatcher);
        CMDataCommand.register(dispatcher);
    }
}
