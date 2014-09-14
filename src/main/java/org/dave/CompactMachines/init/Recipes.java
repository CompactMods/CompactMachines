package org.dave.CompactMachines.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class Recipes
{
    public static void init()
    {

    	GameRegistry.addRecipe(getMachineRecipe(0, "plankWood"));
    	GameRegistry.addRecipe(getMachineRecipe(1, "ingotIron"));
    	GameRegistry.addRecipe(getMachineRecipe(2, "ingotGold"));
    	GameRegistry.addRecipe(getMachineRecipe(3, new ItemStack(Blocks.obsidian)));
    	GameRegistry.addRecipe(getMachineRecipe(4, "gemDiamond"));
    	GameRegistry.addRecipe(getMachineRecipe(5, new ItemStack(Items.nether_star)));

    	GameRegistry.addRecipe(new ShapedOreRecipe(
    			new ItemStack(ModItems.personalShrinkingDevice),
    			"ddd",
    			"xrx",
    			"qqq",
    			'd', "gemDiamond",
    			'x', new ItemStack(ModItems.interfaceItem),
    			'r', "dustRedstone",
    			'q', "gemQuartz"
    	));

    	GameRegistry.addRecipe(new ShapedOreRecipe(
    			new ItemStack(ModBlocks.resizingcube),
    			"xx ",
    			"xdx",
    			" xx",
    			'd', "gemDiamond",
    			'x', new ItemStack(ModItems.interfaceItem)
    	));

    	GameRegistry.addRecipe(new ShapedOreRecipe(
    			new ItemStack(ModItems.interfaceItem),
    			"isi",
    			"rgr",
    			"iei",
    			'i', "ingotIron",
    			'g', "ingotGold",
    			'r', "dustRedstone",
    			's', new ItemStack(ModItems.atomShrinker),
    			'e', new ItemStack(ModItems.atomEnlarger)
    	));

    	GameRegistry.addRecipe(new ShapedOreRecipe(
    			new ItemStack(ModItems.atomShrinker),
    			"ggg",
    			"rpr",
    			"ggg",
    			'g', "blockGlass",
    			'r', "dustRedstone",
    			'p', new ItemStack(Blocks.sticky_piston)
    	));

    	GameRegistry.addRecipe(new ShapedOreRecipe(
    			new ItemStack(ModItems.atomEnlarger),
    			"ggg",
    			"rpr",
    			"ggg",
    			'g', "blockGlass",
    			'r', "dustRedstone",
    			'p', new ItemStack(Blocks.piston)
    	));
    }

    public static ShapedOreRecipe getMachineRecipe(int meta, ItemStack requiredItem) {
    	return new ShapedOreRecipe(
    			new ItemStack(ModBlocks.machine, 1, meta),
    			"ppp",
    			"prp",
    			"ppp",
    			'r', new ItemStack(ModBlocks.resizingcube, 1, 0),
    			'p', requiredItem
    	);
    }

    public static ShapedOreRecipe getMachineRecipe(int meta, String oreName) {
    	return new ShapedOreRecipe(
    			new ItemStack(ModBlocks.machine, 1, meta),
    			"ppp",
    			"prp",
    			"ppp",
    			'r', new ItemStack(ModBlocks.resizingcube, 1, 0),
    			'p', oreName
    	);
    }
}
