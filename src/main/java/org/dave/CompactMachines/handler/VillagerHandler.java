package org.dave.CompactMachines.handler;

import java.util.Random;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import org.dave.CompactMachines.init.ModBlocks;
import org.dave.CompactMachines.init.ModItems;

import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

public class VillagerHandler implements IVillageTradeHandler {
	private static final VillagerHandler instance = new VillagerHandler();

	public static VillagerHandler instance() {
		return instance;
	}

	public void init() {
		VillagerRegistry.instance().registerVillagerId(ConfigurationHandler.villagerId);
		VillagerRegistry.instance().registerVillageTradeHandler(ConfigurationHandler.villagerId, this);
	}

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
		// Interface Item for 3-6 Emeralds
		recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, random.nextInt(3)+3), null, new ItemStack(ModItems.interfaceItem)));

		// Personal Shrinking Device for 5-20 Emeralds
		recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, random.nextInt(15)+5), null, new ItemStack(ModItems.personalShrinkingDevice)));

		// Quantum Entangler for 5-20 Emeralds
		recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, random.nextInt(15)+5), null, new ItemStack(ModItems.quantumEntangler)));

		// World Resizing Cube for 15-35 Emeralds
		recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, random.nextInt(20)+15), null, new ItemStack(ModBlocks.resizingcube)));

	}

}
