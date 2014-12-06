package org.dave.CompactMachines.igw;

import igwmod.api.CraftingRetrievalEvent;
import igwmod.api.WikiRegistry;
import net.minecraftforge.common.MinecraftForge;

import org.dave.CompactMachines.init.ModBlocks;
import org.dave.CompactMachines.init.ModItems;
import org.dave.CompactMachines.init.Recipes;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.utility.LogHelper;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class IGWHandler {
	public static void init() {
		WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.machine, "compactmachines/block.machine");
		WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.interfaceblock, "compactmachines/block.interface");
		WikiRegistry.registerBlockAndItemPageEntry(ModItems.psd, "compactmachines/item.psd");
		WikiRegistry.registerBlockAndItemPageEntry(ModItems.quantumentangler, "compactmachines/item.quantumentangler");

		WikiRegistry.registerBlockAndItemPageEntry(ModItems.enlarger, "compactmachines/item.atom");
		WikiRegistry.registerBlockAndItemPageEntry(ModItems.shrinker, "compactmachines/item.atom");
		WikiRegistry.registerBlockAndItemPageEntry(ModItems.interfaceitem, "compactmachines/item.interfaceItem");

		WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.innerwall, "compactmachines/basics");
		WikiRegistry.registerBlockAndItemPageEntry(ModBlocks.resizingcube, "compactmachines/block.resizingcube");

		WikiRegistry.registerWikiTab(new IGWTab());

		MinecraftForge.EVENT_BUS.register(new IGWHandler());

		LogHelper.info("Hi IGWMod! Pleasure doing business with you.");
	}

	@SubscribeEvent
	public void onIGWRecipe(CraftingRetrievalEvent event) {
		if (event.key.startsWith(Reference.MOD_ID.toLowerCase() + ":machine_")) {
			int meta = -1;
			try {
				meta = Integer.parseInt(event.key.substring((Reference.MOD_ID.toLowerCase() + ":machine_").length()));
			} catch (Exception e) {}

			if (meta > -1 && meta < 6) {
				event.recipe = Recipes.getMachineRecipe(meta);
			}
		}
	}
}
