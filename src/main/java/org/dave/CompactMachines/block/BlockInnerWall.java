package org.dave.CompactMachines.block;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.machines.tools.CubeTools;
import org.dave.CompactMachines.machines.tools.TeleportTools;
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

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int faceHit, float par7, float par8, float par9)
	{
		if (!world.isRemote && player instanceof EntityPlayerMP) {
			if (player.getCurrentEquippedItem() == null && player.isSneaking()) {
				if (world.provider.dimensionId == ConfigurationHandler.dimensionId) {
					EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
					TeleportTools.teleportPlayerBackIPSD(serverPlayer);
					return true;
				}
			}
		}
		return false;
	}
}
