package org.dave.CompactMachines.block;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

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

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		return false;
	}
}
