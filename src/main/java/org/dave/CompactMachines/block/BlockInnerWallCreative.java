package org.dave.CompactMachines.block;

import org.dave.CompactMachines.creativetab.CreativeTabCM;
import org.dave.CompactMachines.reference.Names;

public class BlockInnerWallCreative extends BlockProtected {
	public BlockInnerWallCreative() {
		super();
		this.setBlockName(Names.Blocks.INNERWALL_CREATIVE);
		this.setBlockTextureName(Names.Blocks.INNERWALL);
		this.setCreativeTab(CreativeTabCM.CM_TAB);
	}
}
