package org.dave.CompactMachines.handler;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.init.ModBlocks;
import org.dave.CompactMachines.machines.MachineSaveData;
import org.dave.CompactMachines.network.MessageConfiguration;
import org.dave.CompactMachines.network.PacketHandler;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.utility.LogHelper;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;

public class CMEventHandler {
	@SubscribeEvent
	public void onItemTooltip(ItemTooltipEvent event) {
		if(event.itemStack.getItem() instanceof ItemBlock) {
			ItemBlock itemblock = (ItemBlock)event.itemStack.getItem();
			Block holding = itemblock.field_150939_a;
			if(holding == ModBlocks.interfaceblockcreative || holding == ModBlocks.innerwallcreative) {
				event.toolTip.add(EnumChatFormatting.DARK_GREEN + "-- Creative Mode Fake Block --");
				event.toolTip.add(EnumChatFormatting.RED + "You can only remove these blocks");
				event.toolTip.add(EnumChatFormatting.RED + "while holding an Atom Shrinking");
				event.toolTip.add(EnumChatFormatting.RED + "Module in Creative mode!");
			}
		}
	}

	@SubscribeEvent
	public void onClientConnect(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
		if(!event.isLocal && event.handler instanceof NetHandlerPlayServer) {
			NetHandlerPlayServer nhps = (NetHandlerPlayServer) event.handler;
			EntityPlayerMP player = nhps.playerEntity;

			LogHelper.info("Sending configuration to client: " + player.getDisplayName());
			event.manager.scheduleOutboundPacket(PacketHandler.INSTANCE.getPacketFrom(new MessageConfiguration()));
		}
	}

	@SubscribeEvent
	public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
		if(ConfigurationHandler.isServerConfig) {
			LogHelper.info("Unregistering dimension " + ConfigurationHandler.dimensionId + " on client side");
			DimensionManager.unregisterDimension(ConfigurationHandler.dimensionId);
			DimensionManager.unregisterProviderType(ConfigurationHandler.dimensionId);

			ConfigurationHandler.reload();
			ConfigurationHandler.isServerConfig = false;
			LogHelper.info("Restored original dimension id: " + ConfigurationHandler.dimensionId);
		}
	}

	@SubscribeEvent
	public void loadWorld(WorldEvent.Load event)
	{
		if (!event.world.isRemote && event.world.provider.dimensionId == ConfigurationHandler.dimensionId)
		{
			LogHelper.info("Loading machine world!");
			MachineSaveData machineHandler = (MachineSaveData) event.world.mapStorage.loadData(MachineSaveData.class, "MachineHandler");
			if (machineHandler == null) {
				machineHandler = new MachineSaveData(event.world);
				machineHandler.markDirty();
			}

			machineHandler.setWorld(event.world);

			event.world.mapStorage.setData("MachineHandler", machineHandler);
			CompactMachines.instance.machineHandler = machineHandler;
		}
	}

	@SubscribeEvent
	public void playerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		if(event.toDim != ConfigurationHandler.dimensionId) {
			return;
		}

		if(event.player == null) {
			return;
		}

		if(ConfigurationHandler.allowEnterWithoutPSD) {
			return;
		}

		NBTTagCompound playerNBT = event.player.getEntityData();
		if(!playerNBT.getBoolean("isUsingPSD")) {
			event.player.addPotionEffect(new PotionEffect(Potion.wither.id, 300, 2, false));	// Wither
			event.player.addPotionEffect(new PotionEffect(Potion.confusion.id, 300, 5, false)); // Nausea
		} else {
			playerNBT.removeTag("isUsingPSD");
		}
	}

	@SubscribeEvent
	public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
		if(event.modID.equals(Reference.MOD_ID)) {
			ConfigurationHandler.saveConfiguration();
		}
	}
}
