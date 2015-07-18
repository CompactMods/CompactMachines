package org.dave.CompactMachines.block;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.dave.CompactMachines.machines.tools.CubeTools;
import org.dave.CompactMachines.reference.Names;

public class BlockInnerWall extends BlockProtected {
	public BlockInnerWall()
	{
		super();
		this.setBlockName(Names.Blocks.INNERWALL);
		this.setBlockTextureName(Names.Blocks.INNERWALL);
		this.setLightOpacity(1);
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		return CubeTools.shouldSideBeRendered(world, x, y, z, side);
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	// Prevent blocks from being placed by players
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		world.setBlockToAir(x, y, z);
		return;
	}
}
