package org.dave.CompactMachines.block;

import org.dave.CompactMachines.creativetab.CreativeTabCM;
import org.dave.CompactMachines.reference.Names;

public class BlockInterfaceCreative extends BlockProtected {
	public BlockInterfaceCreative() {
		super();
		this.setBlockName(Names.Blocks.INTERFACE_CREATIVE);
		this.setBlockTextureName(Names.Blocks.INTERFACE);
		this.setCreativeTab(CreativeTabCM.CM_TAB);
	}
}
