package org.dave.CompactMachines.block;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.dave.CompactMachines.item.ItemAtomShrinker;
import org.dave.CompactMachines.reference.Names;

public class BlockInnerWall extends BlockCM {
	public BlockInnerWall()
	{
		super();
		this.setBlockName(Names.Blocks.INNERWALL);
		this.setBlockTextureName(Names.Blocks.INNERWALL);
		this.setBlockUnbreakable();
		this.setResistance(6000000.0F);
		this.setLightOpacity(1);
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		if (!world.isRemote && player instanceof EntityPlayerMP && player.capabilities.isCreativeMode) {
			ItemStack playerStack = player.getCurrentEquippedItem();
			if(playerStack != null && playerStack.getItem() instanceof ItemAtomShrinker) {
				return super.removedByPlayer(world, player, x, y, z);
			}
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		world.setBlockToAir(x, y, z);
		return;
	}
}
