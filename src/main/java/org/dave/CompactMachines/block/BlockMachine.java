package org.dave.CompactMachines.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.item.ItemPersonalShrinkingDevice;
import org.dave.CompactMachines.reference.GuiId;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.dave.CompactMachines.utility.FluidUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMachine extends BlockCM implements ITileEntityProvider
{
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public BlockMachine()
	{
		super();
		this.setBlockName(Names.Blocks.MACHINE);
		this.setBlockTextureName(Names.Blocks.MACHINE);
		this.setHardness(4.0F);
		this.setResistance(6000000.0F);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister) {
        icons = new IIcon[6];

        for (int i = 0; i < icons.length; i++)
        {
            icons[i] = iconRegister.registerIcon("compactmachines:machine_" + i);
        }
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		return icons[metadata];
	}


	@Override
	public TileEntity createNewTileEntity(World world, int metaData) {
		return new TileEntityMachine();
	}

	@Override
	public boolean hasTileEntity(int metadata) {
	    return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item unknown, CreativeTabs tab, List subItems) {
		for (int i = 0; i < 6; i++) {
			subItems.add(new ItemStack(this, 1, i));
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if((world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
			TileEntityMachine tileEntityMachine = (TileEntityMachine)world.getTileEntity(x, y, z);

			// TODO: Implement a limit on how deep block breaking can recurse!
			// And while you are at it, reduce drop chance the deeper in the item comes from.
			CompactMachines.instance.machineHandler.harvestMachine(tileEntityMachine);

			// Disable chunk loading and remove it from the worlds NBT table
			CompactMachines.instance.machineHandler.disableMachine(tileEntityMachine);
		}

		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int faceHit, float par7, float par8, float par9)
	{
		if (player.isSneaking())
		{
			return false;
		}
		else
		{
			if (!world.isRemote && player instanceof EntityPlayerMP)
			{
				if (!(world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
					return false;
				}

				TileEntityMachine tank = (TileEntityMachine) world.getTileEntity(x, y, z);
				ItemStack playerStack = player.getCurrentEquippedItem();
				// First check if the player is right clicking with a shrinker
				if(playerStack != null && playerStack.getItem() instanceof ItemPersonalShrinkingDevice) {
					// Activated with a PSD
					CompactMachines.instance.machineHandler.teleportPlayerToMachineWorld((EntityPlayerMP)player, tank);
				} else if(playerStack != null && FluidContainerRegistry.isEmptyContainer(playerStack)) {
					// Activated with an empty bucket
					FluidUtils.emptyTankIntoContainer(tank, player, tank.getFluid(faceHit), ForgeDirection.getOrientation(faceHit));
				} else if(playerStack != null && FluidContainerRegistry.isFilledContainer(playerStack)) {
					// Activated with a filled bucket
					FluidUtils.fillTankWithContainer(tank, player, ForgeDirection.getOrientation(faceHit));
				} else {
					player.openGui(CompactMachines.instance, GuiId.MACHINE.ordinal(), world, x, y, z);
				}
			}

			return true;
		}
	}

	@Override
	public int damageDropped(int meta) {
		return meta;
	}
}
