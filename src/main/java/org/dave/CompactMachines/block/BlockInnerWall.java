package org.dave.CompactMachines.block;

import org.dave.CompactMachines.reference.Names;

public class BlockInnerWall extends BlockCM {
	public BlockInnerWall()
	{
		super();
		this.setBlockName(Names.Blocks.INNERWALL);
		this.setBlockTextureName(Names.Blocks.INNERWALL);
		this.setBlockUnbreakable();
		this.setResistance(6000000.0F);
	}
}
