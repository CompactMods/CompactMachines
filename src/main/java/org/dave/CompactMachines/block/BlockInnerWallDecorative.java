package org.dave.CompactMachines.block;

import org.dave.CompactMachines.creativetab.CreativeTabCM;
import org.dave.CompactMachines.reference.Names;

public class BlockInnerWallDecorative extends BlockCM {
	public BlockInnerWallDecorative() {
		super();
		this.setBlockName(Names.Blocks.INNERWALL_DECORATIVE);
		this.setBlockTextureName(Names.Blocks.INNERWALL);
		this.setHardness(8.0F);
		this.setResistance(20.0F);
		this.setCreativeTab(CreativeTabCM.CM_TAB);
	}


}
