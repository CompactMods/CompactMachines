package org.dave.CompactMachines.block;

import org.dave.CompactMachines.reference.Names;

public class BlockInnerWall extends BlockProtected {
	public BlockInnerWall()
	{
		super();
		this.setBlockName(Names.Blocks.INNERWALL);
		this.setBlockTextureName(Names.Blocks.INNERWALL);
		this.setLightOpacity(1);
	}
}
