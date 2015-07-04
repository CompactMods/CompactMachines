package org.dave.CompactMachines.block;

import net.minecraft.world.IBlockAccess;

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
}
