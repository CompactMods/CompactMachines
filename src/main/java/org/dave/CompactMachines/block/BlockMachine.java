package org.dave.CompactMachines.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
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
	private IIcon[] iconsUpg;

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
        iconsUpg = new IIcon[6];

        for (int i = 0; i < icons.length; i++)
        {
            icons[i] = iconRegister.registerIcon("compactmachines:machine_" + i);
            iconsUpg[i] = iconRegister.registerIcon("compactmachines:machine_" + i + "_upg");
        }
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata) {
		return icons[metadata];
	}


	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(IBlockAccess blockAccess, int x, int y, int z, int side) {
		TileEntityMachine tileEntityMachine = (TileEntityMachine)blockAccess.getTileEntity(x, y, z);
		if(tileEntityMachine == null) {
			return icons[0];
		} else {
			if(tileEntityMachine.isUpgraded) {
				return iconsUpg[tileEntityMachine.meta];
			} else {
				return icons[tileEntityMachine.meta];
			}
		}
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metaData) {
		TileEntityMachine tileEntityMachine = new TileEntityMachine();
		tileEntityMachine.meta = metaData;
		return tileEntityMachine;
	}

	@Override
	public boolean hasTileEntity(int metadata) {
	    return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, player, stack);

		if(stack.stackTagCompound == null) {
			return;
		}

		int coords = stack.stackTagCompound.getInteger("coords");
		//LogHelper.info("Placing block with coords: " + coords);
		if((world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
			TileEntityMachine tileEntityMachine = (TileEntityMachine)world.getTileEntity(x, y, z);
			if(tileEntityMachine.coords == -1) {
				tileEntityMachine.coords = coords;
				tileEntityMachine.isUpgraded = true;
				tileEntityMachine.meta = stack.getItemDamage();
				tileEntityMachine.markDirty();
				CompactMachines.instance.machineHandler.forceChunkLoad(coords);
			}
		}

	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return new ArrayList<ItemStack>();
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

			if(!tileEntityMachine.isUpgraded) {
				CompactMachines.instance.machineHandler.harvestMachine(tileEntityMachine);
			}

			// Disable chunk loading and remove it from the worlds NBT table
			CompactMachines.instance.machineHandler.disableMachine(tileEntityMachine);

			tileEntityMachine.dropAsItem();
			world.removeTileEntity(x,y,z);
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

				TileEntityMachine tileEntityMachine = (TileEntityMachine) world.getTileEntity(x, y, z);
				ItemStack playerStack = player.getCurrentEquippedItem();

				// First check if the player is right clicking with a shrinker
				if(playerStack != null && playerStack.getItem() instanceof ItemPersonalShrinkingDevice) {
					// Activated with a PSD
					CompactMachines.instance.machineHandler.teleportPlayerToMachineWorld((EntityPlayerMP)player, tileEntityMachine);
				} else if(playerStack != null && FluidContainerRegistry.isEmptyContainer(playerStack)) {
					// Activated with an empty bucket
					FluidUtils.emptyTankIntoContainer(tileEntityMachine, player, tileEntityMachine.getFluid(faceHit), ForgeDirection.getOrientation(faceHit));
				} else if(playerStack != null && FluidContainerRegistry.isFilledContainer(playerStack)) {
					// Activated with a filled bucket
					FluidUtils.fillTankWithContainer(tileEntityMachine, player, ForgeDirection.getOrientation(faceHit));
				} else if(tileEntityMachine.isUpgraded == false && playerStack != null && playerStack.getItem() == Items.nether_star) {
					// Activated with a nether star
					tileEntityMachine.isUpgraded = true;
					tileEntityMachine.markDirty();

					world.markBlockForUpdate(x, y, z);

					playerStack.stackSize--;
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
