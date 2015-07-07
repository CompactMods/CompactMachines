package org.dave.CompactMachines.init;

import org.dave.CompactMachines.block.BlockCM;
import org.dave.CompactMachines.block.BlockInnerWall;
import org.dave.CompactMachines.block.BlockInnerWallDecorative;
import org.dave.CompactMachines.block.BlockInterface;
import org.dave.CompactMachines.block.BlockInterfaceDecorative;
import org.dave.CompactMachines.block.BlockMachine;
import org.dave.CompactMachines.block.BlockResizingCube;
import org.dave.CompactMachines.item.ItemBlockMachine;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ModBlocks
{
	public static final BlockCM	machine				= new BlockMachine();
	public static final BlockCM	interfaceblock		= new BlockInterface();
	public static final BlockCM	interfaceblockdecor	= new BlockInterfaceDecorative();
	public static final BlockCM	innerwall			= new BlockInnerWall();
	public static final BlockCM	innerwalldecor		= new BlockInnerWallDecorative();
	public static final BlockCM	resizingcube		= new BlockResizingCube();

	public static void init()
	{
		GameRegistry.registerBlock(machine, ItemBlockMachine.class, Names.Blocks.MACHINE);
		GameRegistry.registerBlock(interfaceblock, Names.Blocks.INTERFACE);
		GameRegistry.registerBlock(interfaceblockdecor, Names.Blocks.INTERFACE_DECORATIVE);
		GameRegistry.registerBlock(innerwall, Names.Blocks.INNERWALL);
		GameRegistry.registerBlock(innerwalldecor, Names.Blocks.INNERWALL_DECORATIVE);
		GameRegistry.registerBlock(resizingcube, Names.Blocks.RESIZINGCUBE);
	}
}
