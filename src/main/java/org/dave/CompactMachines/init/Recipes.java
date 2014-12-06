package org.dave.CompactMachines.init;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

public class Recipes
{
	public static void init()
	{

		GameRegistry.addRecipe(getMachineRecipe(0));
		GameRegistry.addRecipe(getMachineRecipe(1));
		GameRegistry.addRecipe(getMachineRecipe(2));
		GameRegistry.addRecipe(getMachineRecipe(3));
		GameRegistry.addRecipe(getMachineRecipe(4));
		GameRegistry.addRecipe(getMachineRecipe(5));

		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(ModItems.psd),
				"ddd",
				"xrx",
				"qqq",
				'd', "gemDiamond",
				'x', new ItemStack(ModItems.interfaceitem),
				'r', "dustRedstone",
				'q', "gemQuartz"
				));

		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(ModBlocks.resizingcube),
				"xx ",
				"xdx",
				" xx",
				'd', "gemDiamond",
				'x', new ItemStack(ModItems.interfaceitem)
				));

		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(ModBlocks.innerwalldecor, 8),
				"xxx",
				"xdx",
				"xxx",
				'd', new ItemStack(ModItems.enlarger),
				'x', "plankWood"
				));

		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(ModBlocks.innerwalldecor, 8),
				"xxx",
				"xdx",
				"xxx",
				'd', new ItemStack(ModItems.shrinker),
				'x', "plankWood"
				));

		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(ModItems.interfaceitem),
				"isi",
				"rgr",
				"iei",
				'i', "ingotIron",
				'g', "ingotGold",
				'r', "dustRedstone",
				's', new ItemStack(ModItems.shrinker),
				'e', new ItemStack(ModItems.enlarger)
				));

		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(ModItems.shrinker),
				"ggg",
				"rpr",
				"ggg",
				'g', "blockGlass",
				'r', "dustRedstone",
				'p', new ItemStack(Blocks.sticky_piston)
				));

		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(ModItems.enlarger),
				"ggg",
				"rpr",
				"ggg",
				'g', "blockGlass",
				'r', "dustRedstone",
				'p', new ItemStack(Blocks.piston)
				));

		GameRegistry.addRecipe(new ShapedOreRecipe(
				new ItemStack(ModItems.quantumentangler),
				"qqq",
				"xsx",
				"qqq",
				'q', "gemQuartz",
				'x', new ItemStack(ModItems.interfaceitem),
				's', new ItemStack(Items.nether_star)
				));
	}

	public static ShapedOreRecipe getMachineRecipe(int meta) {
		switch (meta) {
			case 0:
				return getMachineRecipe(meta, "plankWood");
			case 1:
				return getMachineRecipe(meta, "ingotIron");
			case 2:
				return getMachineRecipe(meta, "ingotGold");
			case 3:
				return getMachineRecipe(meta, new ItemStack(Blocks.obsidian));
			case 4:
				return getMachineRecipe(meta, "gemDiamond");
			case 5:
				return getMachineRecipe(meta, new ItemStack(Items.nether_star));
			default:
				break;
		}

		return null;
	}

	public static ShapedOreRecipe getMachineRecipe(int meta, ItemStack requiredItem) {
		return new ShapedOreRecipe(
				new ItemStack(ModBlocks.machine, 1, meta),
				"ppp",
				"prp",
				"ppp",
				'r', new ItemStack(ModBlocks.resizingcube, 1, 0),
				'p', requiredItem);
	}

	public static ShapedOreRecipe getMachineRecipe(int meta, String oreName) {
		return new ShapedOreRecipe(
				new ItemStack(ModBlocks.machine, 1, meta),
				"ppp",
				"prp",
				"ppp",
				'r', new ItemStack(ModBlocks.resizingcube, 1, 0),
				'p', oreName);
	}
}
