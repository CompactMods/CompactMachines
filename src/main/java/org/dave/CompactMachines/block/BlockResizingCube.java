package org.dave.CompactMachines.block;

import org.dave.CompactMachines.reference.Names;

public class BlockResizingCube extends BlockCM {
	public BlockResizingCube() {
		super();
		this.setBlockName(Names.Blocks.RESIZINGCUBE);
		this.setBlockTextureName(Names.Blocks.RESIZINGCUBE);
		this.setHardness(16.0F);
		this.setResistance(20.0F);
	}	
}
