package dev.compactmods.machines.client;

import dev.compactmods.machines.client.machine.MachineScreenPanels;
import dev.compactmods.machines.upgrades.client.StorageRenderers;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.command.client.CMClientCommands;
import dev.compactmods.machines.client.config.ClientConfig;
import dev.compactmods.machines.client.creative.CreativeTabs;
import dev.compactmods.machines.machine.client.MachinesClient;
import dev.compactmods.machines.client.room.RoomsClient;
import dev.compactmods.machines.preview.client.RoomPreviewClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = CompactMachinesCore.MOD_ID, dist = Dist.CLIENT)
public class CompactMachinesClient {

   public CompactMachinesClient(ModContainer modContainer, IEventBus modBus) {
	  modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.CONFIG);

	  CreativeTabs.prepare();

	  registerEvents(modBus);
   }

   public static void registerEvents(IEventBus modBus) {
	  MachinesClient.registerEvents(modBus);
	  RoomsClient.registerEvents(modBus);
	  RoomPreviewClient.registerEvents(modBus);
	  MachineScreenPanels.PANELS.register(modBus);
	  StorageRenderers.register(modBus);

	  NeoForge.EVENT_BUS.addListener(CMClientCommands::registerClientCommands);
   }

   private static void registerShaders(RegisterRenderPipelinesEvent pipelinesEvent) {

   }
}
