package org.dave.CompactMachines.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.block.BlockMachine;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.machines.tools.TeleportTools;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.tileentity.TileEntityMachine;

public class ItemPersonalShrinkingDevice extends ItemCM
{
	public ItemPersonalShrinkingDevice()
	{
		super();
		this.setUnlocalizedName(Names.Items.PSD);
		this.setMaxStackSize(1);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer) {
		if (!world.isRemote && entityPlayer instanceof EntityPlayerMP) {
			EntityPlayerMP serverPlayer = (EntityPlayerMP) entityPlayer;

			if (world.provider.dimensionId == ConfigurationHandler.dimensionId) {
				if (serverPlayer.isSneaking()) {
					CompactMachines.instance.machineHandler.setCoordSpawnpoint(serverPlayer);
					serverPlayer.addChatMessage(new ChatComponentTranslation("msg.message_spawnpoint_set.txt"));
					return itemStack;
				}

				TeleportTools.teleportPlayerBack(serverPlayer);
			}
		}
		return itemStack;
	}

	@Override
	public boolean onItemUseFirst(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (!player.isSneaking() || world.isRemote) {
			return false;
		}

		Block block = world.getBlock(x, y, z);
		if (!(block instanceof BlockMachine)) {
			return false;
		}

		TileEntityMachine tileEntityMachine = (TileEntityMachine) world.getTileEntity(x, y, z);
		if (tileEntityMachine.hasIntegratedPSD) {
			return false;
		}

		tileEntityMachine.hasIntegratedPSD = true;
		tileEntityMachine.markDirty();
		world.markBlockForUpdate(x, y, z);
		itemStack.stackSize--;
		return true;
	}
}
