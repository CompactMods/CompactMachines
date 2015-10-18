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
	public static int			capacityEssentia;
	public static int			cooldownRF;
	public static int			cooldownItems;
	public static int			cooldownFluid;
	public static int			cooldownGas;
	public static int			cooldownEssentia;
	public static String		upgradeItem;
	public static boolean		allowEntanglement;
	public static boolean		keepPlayersInsideOfRooms;
	public static int			villagerId;
	public static boolean		enableVillager;
	public static boolean		allowEnterWithoutPSD;

	public static boolean		enableIntegrationLookingGlass;
	public static int			psdDisplayColor;
	public static int			psdResolutionX;
	public static int			psdResolutionY;

	public static boolean		enableIntegrationAE2;
	public static boolean		enableIntegrationBotania;
	public static boolean		enableIntegrationMekanism;
	public static boolean		enableIntegrationProjectRed;
	public static boolean		enableIntegrationOpenComputers;
	public static boolean		enableIntegrationThaumcraft;
	public static boolean		enableIntegrationPneumaticCraft;

	public static boolean		allowRespawning;

	public static boolean		adaptBiomes;
	public static String		defaultBiome;

	public static boolean		isServerConfig;

	public static void init(File configFile) {
		// Create the configuration object from the given configuration file
		if (configuration == null) {
			/*
			File serverConfig = new File(configFile.getPath().replaceAll(".cfg", "-server.cfg"));
			if(FMLCommonHandler.instance().getEffectiveSide().isServer() && serverConfig.exists()) {
				LogHelper.info("Using server config file: " + serverConfig.getPath());
				configFile = serverConfig;
			}
			*/
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

		enableIntegrationAE2 = configuration.getBoolean("AppliedEnergistics", "Integration", true, "Allow AE2 connections through Compact Machines");
		enableIntegrationBotania = configuration.getBoolean("Botania", "Integration", false, "Compact Machines can transfer Mana. This is not sided, i.e. all interfaces share the same amount of mana.");
		enableIntegrationMekanism = configuration.getBoolean("Mekanism", "Integration", true, "Transfer Mekanism Gas");
		enableIntegrationProjectRed = configuration.getBoolean("ProjectRed", "Integration", true, "Transfer bundled cable signals through Compact Machines");
		enableIntegrationOpenComputers = configuration.getBoolean("OpenComputers", "Integration", true, "Allow OpenComputers network connections through Compact Machines");
		enableIntegrationThaumcraft = configuration.getBoolean("Thaumcraft", "Integration", true, "Allow Thaumcraft Essentia transport through Compact Machines");
		enableIntegrationPneumaticCraft = configuration.getBoolean("PneumaticCraft", "Integration", true, "Allow PneumaticCraft Pressure transport through Compact Machines");

		cooldownRF = configuration.getInt("cooldownRF", "CompactMachines", 0, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Redstone Flux, i.e. 20 => 10000 RF/s, 0 => 10000 RF/t");
		cooldownItems = configuration.getInt("cooldownItems", "CompactMachines", 10, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Items, i.e. 40 => 1 Stack every two seconds");
		cooldownFluid = configuration.getInt("cooldownFluid", "CompactMachines", 10, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Fluids, i.e. 0 => 1 Bucket per tick");
		cooldownGas = configuration.getInt("cooldownGas", "CompactMachines", 0, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Gases, i.e. 0 => 1024 units per tick");
		cooldownEssentia = configuration.getInt("cooldownEssentia", "CompactMachines", 0, 0, Integer.MAX_VALUE, "Number of ticks between each import/export action for Essentia, i.e. 0 => 1 Essentia per tick");

		capacityRF = configuration.getInt("capacityRF", "CompactMachines", 10000, 0, Integer.MAX_VALUE, "Maximum amount of RF a CM buffer can hold.");
		capacityFluid = configuration.getInt("capacityFluid", "CompactMachines", 1000, 0, Integer.MAX_VALUE, "Maximum amount of fluid (in mB) a CM buffer can hold.");
		capacityGas = configuration.getInt("capacityGas", "CompactMachines", 1024, 0, Integer.MAX_VALUE, "Maximum amount of gas a CM buffer can hold.");
		capacityMana = configuration.getInt("capacityMana", "CompactMachines", 10000, 0, Integer.MAX_VALUE, "Maximum amount of Botania Mana a CM buffer can hold.");
		capacityEssentia = configuration.getInt("capacityEssentia", "CompactMachines", 64, 0, Integer.MAX_VALUE, "Maximum amount of Thaumcraft Essentia a CM buffer can hold.");

		int red = configuration.getInt("psdDisplayColor.red", "Rendering", 0x27, 0, Integer.MAX_VALUE, "Font color for the PSD");
		int green = configuration.getInt("psdDisplayColor.green", "Rendering", 0xEB, 0, Integer.MAX_VALUE, "");
		int blue = configuration.getInt("psdDisplayColor.blue", "Rendering", 0xF5, 0, Integer.MAX_VALUE, "");

		psdDisplayColor = (red << 16) + (green << 8) + blue;

		enableIntegrationLookingGlass = configuration.getBoolean("LookingGlass", "Rendering", false, "Use Looking Glass to render previews of CM contents.");
		psdResolutionX = configuration.getInt("psdDisplayResolution.x", "Rendering", 160, 40, 640, "[LookingGlass] Horizontal resolution");
		psdResolutionY = configuration.getInt("psdDisplayResolution.y", "Rendering", 120, 30, 480, "[LookingGlass] Vertical resolution");

		upgradeItem = configuration.getString("upgradeItem", "CompactMachines", "nether_star", "The item used to upgrade compact machines. Format: modid:name_block_registered_with");

		enableVillager = configuration.getBoolean("enableVillager", "CompactMachines", true, "Enables Villager spawns trading PSDs, QEs and World Resizing Cubes.");

		allowEnterWithoutPSD = configuration.getBoolean("allowEnterWithoutPSD", "CompactMachines", false, "Allow players to enter Compact Machines by other means than a PSD. Gives wither effect and nausea if disallowed.");

		allowRespawning = configuration.getBoolean("allowRespawn", "CompactMachines", false, "Allow players to respawn inside of Compact Machines and place their beds there. NOTE: Vanilla currently does not make you change dimensions on death.");

		adaptBiomes = configuration.getBoolean("adaptBiomes", "CompactMachines", true, "If set to true the CMs biome is the same as the spot it has been placed in. Otherwise uses the default.");
		defaultBiome = configuration.getString("defaultBiome", "CompactMachines", "sky", "The biome to use when biome adaptation is disabled.");

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
