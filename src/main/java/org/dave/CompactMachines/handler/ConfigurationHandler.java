package org.dave.CompactMachines.handler;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ConfigurationHandler {
	public static Configuration	configuration;
	public static boolean		testValue	= false;
	public static int			dimensionId;
	public static int			maxDroppedStacks;
	public static int			chunkLoadingMode;
	public static int			cubeDistance;
	public static int			capacityRF;
	public static int			capacityFluid;
	public static int			capacityGas;
	public static int			capacityMana;
	public static int			cooldownRF;
	public static int			cooldownItems;
	public static int			cooldownFluid;
	public static int			cooldownGas;
	public static String		upgradeItem;
	public static boolean		allowEntanglement;
	public static boolean		keepPlayersInsideOfRooms;
	public static int			villagerId;
	public static boolean		enableVillager;
	public static boolean		allowEnterWithoutPSD;
	public static int			psdDisplayColor;

	public static boolean		isServerConfig;

	public static void init(File configFile) {
		// Create the configuration object from the given configuration file
		if (configuration == null) {
			configuration = new Configuration(configFile);
			loadConfiguration();
		}
	}

	public static void reload() {
		if(configuration != null) {
			loadConfiguration();
		}
	}

	private static void loadConfiguration() {
		dimensionId = configuration.getInt("dimension", "Internal", -1, Integer.MIN_VALUE, Integer.MAX_VALUE, "Dimension used for machines. Do not change this unless it is somehow conflicting!");
		cubeDistance = configuration.getInt("cubeDistance", "Internal", 64, 16, Integer.MAX_VALUE, "The distance between the cubes in the machine dimension! Must be a multiple of 16, i.e. 16, 32, 48, 64... DO NOT CHANGE THIS.");
		villagerId = configuration.getInt("villagerId", "Internal", 64, 16, Integer.MAX_VALUE, "ID used for the Compact Machines villager. Change if id should collide with another mod adding villagers.");

		maxDroppedStacks = configuration.getInt("maxDroppedStacks", "CompactMachines", 128, 0, Integer.MAX_VALUE, "Maximum number of items dropping when breaking a Compact Machine");
		chunkLoadingMode = configuration.getInt("chunkLoadingMode", "CompactMachines", 1, 0, 2, "Chunk Loading Mode: 0 = Never, 1 = Always, 2 = When machine is loaded");
		allowEntanglement = configuration.getBoolean("allowEntanglement", "CompactMachines", true, "Allow entangling of Compact Machines. This is very powerful as it makes AE2s Quantum Network Bridges obsolete for example.");
		keepPlayersInsideOfRooms = configuration.getBoolean("keepPlayersInsideOfRooms", "CompactMachines", true, "Prevent players from leaving a Compact Machines room boundaries.");

		cooldownRF = configuration.getInt("cooldownRF", "CompactMachines", 0, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Redstone Flux, i.e. 20 => 10000 RF/s, 0 => 10000 RF/t");
		cooldownItems = configuration.getInt("cooldownItems", "CompactMachines", 10, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Items, i.e. 40 => 1 Stack every two seconds");
		cooldownFluid = configuration.getInt("cooldownFluid", "CompactMachines", 10, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Fluids, i.e. 0 => 1 Bucket per tick");
		cooldownGas = configuration.getInt("cooldownGas", "CompactMachines", 0, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Gases, i.e. 0 => 1024 units per tick");

		capacityRF = configuration.getInt("capacityRF", "CompactMachines", 10000, 0, Integer.MAX_VALUE, "Maximum amount of RF a CM buffer can hold.");
		capacityFluid = configuration.getInt("capacityFluid", "CompactMachines", 1000, 0, Integer.MAX_VALUE, "Maximum amount of fluid (in mB) a CM buffer can hold.");
		capacityGas = configuration.getInt("capacityGas", "CompactMachines", 1024, 0, Integer.MAX_VALUE, "Maximum amount of gas a CM buffer can hold.");
		capacityMana = configuration.getInt("capacityMana", "CompactMachines", 10000, 0, Integer.MAX_VALUE, "Maximum amount of Botania Mana a CM buffer can hold.");

		int red = configuration.getInt("psdDisplayColor.red", "Rendering", 0x27, 0, Integer.MAX_VALUE, "Font color for the PSD");
		int green = configuration.getInt("psdDisplayColor.green", "Rendering", 0xEB, 0, Integer.MAX_VALUE, "");
		int blue = configuration.getInt("psdDisplayColor.blue", "Rendering", 0xF5, 0, Integer.MAX_VALUE, "");

		psdDisplayColor = (red << 16) + (green << 8) + blue;

		upgradeItem = configuration.getString("upgradeItem", "CompactMachines", "nether_star", "The item used to upgrade compact machines. Format: modid:name_block_registered_with");

		enableVillager = configuration.getBoolean("enableVillager", "CompactMachines", true, "Enables Villager spawns trading PSDs, QEs and World Resizing Cubes.");

		allowEnterWithoutPSD = configuration.getBoolean("allowEnterWithoutPSD", "CompactMachines", false, "Allow players to enter Compact Machines by other means than a PSD. Gives wither effect and nausea if disallowed.");

		if (configuration.hasChanged()) {
			configuration.save();
		}
	}

	public static void saveConfiguration() {
		if(isServerConfig) {
			return;
		}

		Property dimProp = configuration.get("Internal", "dimension", -1, "Dimension used for machines. Do not change this unless it is somehow conflicting!");
		dimProp.set(dimensionId);

		if (configuration.hasChanged()) {
			configuration.save();
		}
	}

	@SubscribeEvent
	public void onConfigurationChangedEvent(ConfigChangedEvent.OnConfigChangedEvent event) {
		if (event.modID.equalsIgnoreCase(Reference.MOD_ID)) {
			loadConfiguration();
		}
	}
}
