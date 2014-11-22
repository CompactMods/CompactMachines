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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.creativetab.CreativeTabCM;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.init.ModItems;
import org.dave.CompactMachines.item.ItemPersonalShrinkingDevice;
import org.dave.CompactMachines.reference.GuiId;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.dave.CompactMachines.utility.FluidUtils;
import org.dave.CompactMachines.utility.WorldUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMachine extends BlockCM implements ITileEntityProvider
{
	private boolean forceTeleport;

	@SideOnly(Side.CLIENT)
	private IIcon[]	icons;
	private IIcon[]	iconsUpg;

	public BlockMachine()
	{
		super();
		this.setBlockName(Names.Blocks.MACHINE);
		this.setBlockTextureName(Names.Blocks.MACHINE);
		this.setHardness(4.0F);
		this.setResistance(6000000.0F);
		this.setCreativeTab(CreativeTabCM.CM_TAB);
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
		TileEntityMachine tileEntityMachine = (TileEntityMachine) blockAccess.getTileEntity(x, y, z);
		if (tileEntityMachine == null) {
			return icons[0];
		} else {
			if (tileEntityMachine.isUpgraded) {
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

		WorldUtils.updateNeighborAEGrids(world, x, y, z);

		if (stack.stackTagCompound == null) {
			return;
		}

		if (!(world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
			return;
		}

		TileEntityMachine tileEntityMachine = (TileEntityMachine) world.getTileEntity(x, y, z);
		if (tileEntityMachine.coords != -1) {
			// The machine already has data for some reason
			return;
		}

		if (stack.stackTagCompound.hasKey("coords")) {
			int coords = stack.stackTagCompound.getInteger("coords");
			if (coords != -1) {
				tileEntityMachine.coords = coords;
				tileEntityMachine.isUpgraded = true;
			}
		}

		if (stack.hasDisplayName()) {
			tileEntityMachine.setCustomName(stack.getDisplayName());
		}

		tileEntityMachine.markDirty();
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
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		if (willHarvest && (world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
			final TileEntityMachine tileEntityMachine = (TileEntityMachine) world.getTileEntity(x, y, z);

			boolean result = super.removedByPlayer(world, player, x, y, z, willHarvest);

			if (result) {
				if (!tileEntityMachine.isUpgraded) {
					CompactMachines.instance.machineHandler.harvestMachine(tileEntityMachine, player);
				}

				tileEntityMachine.dropAsItem();
			}

			return result;
		}

		return super.removedByPlayer(world, player, x, y, z, willHarvest);
	};

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if ((world.getTileEntity(x, y, z) instanceof TileEntityMachine)) {
			TileEntityMachine tileEntityMachine = (TileEntityMachine) world.getTileEntity(x, y, z);

			// Disable chunk loading and remove it from the worlds NBT table
			CompactMachines.instance.machineHandler.disableMachine(tileEntityMachine);

			world.removeTileEntity(x, y, z);
		}

		WorldUtils.updateNeighborAEGrids(world, x, y, z);
	}
 	
	public boolean getForceTeleport() { 	
		return forceTeleport; 	
	} 	
	
	public void setForceTeleport( boolean value ) { 	
		forceTeleport = value; 	
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

				// XXX: Do we need to do anything for gases here?

				// First check if the force teleport flag is on or the player is right clicking with a shrinker
				if (forceTeleport || playerStack != null && playerStack.getItem() instanceof ItemPersonalShrinkingDevice) {
					// Activated with a PSD
					CompactMachines.instance.machineHandler.teleportPlayerToMachineWorld((EntityPlayerMP) player, tileEntityMachine);
				} else if (playerStack != null && FluidContainerRegistry.isEmptyContainer(playerStack)) {
					// Activated with an empty bucket
					FluidUtils.emptyTankIntoContainer(tileEntityMachine, player, tileEntityMachine.getFluid(faceHit), ForgeDirection.getOrientation(faceHit));
				} else if (playerStack != null && FluidContainerRegistry.isFilledContainer(playerStack)) {
					// Activated with a filled bucket
					FluidUtils.fillTankWithContainer(tileEntityMachine, player, ForgeDirection.getOrientation(faceHit));
				} else if (tileEntityMachine.isUpgraded == false && playerStack != null && playerStack.getItem() == Reference.upgradeItem) {
					// Activated with a nether star
					tileEntityMachine.isUpgraded = true;
					tileEntityMachine.markDirty();

					world.markBlockForUpdate(x, y, z);

					playerStack.stackSize--;
				} else if (playerStack != null && playerStack.getItem() == ModItems.quantumEntangler) {

					if (!ConfigurationHandler.allowEntanglement) {
						player.addChatMessage(new ChatComponentTranslation("msg.message_quantum_entanglement_disabled.txt"));
						return true;
					}

					if (playerStack.hasTagCompound() && playerStack.getTagCompound().hasKey("coords") && playerStack.getTagCompound().hasKey("size")) {
						// quantumEntangler already has a compound
						if (tileEntityMachine.coords != -1) {
							player.addChatMessage(new ChatComponentTranslation("msg.message_machine_already_in_use.txt"));
						} else if (tileEntityMachine.isUpgraded == false) {
							player.addChatMessage(new ChatComponentTranslation("msg.message_machine_not_upgraded.txt"));
						} else {
							NBTTagCompound stackNbt = playerStack.getTagCompound();

							int size = stackNbt.getInteger("size");
							if (size != tileEntityMachine.meta) {
								player.addChatMessage(new ChatComponentTranslation("msg.message_machine_invalid_size.txt"));
							} else {
								int coords = stackNbt.getInteger("coords");
								tileEntityMachine.coords = coords;

								if (stackNbt.hasKey("roomname")) {
									tileEntityMachine.setCustomName(stackNbt.getString("roomname"));
								}

								tileEntityMachine.markDirty();

								playerStack.stackSize--;
							}
						}

					} else if (tileEntityMachine.isUpgraded && tileEntityMachine.coords != -1) {
						// No "coords" tag yet and the machine is in use and upgraded
						// --> Save the coords
						NBTTagCompound nbt = new NBTTagCompound();
						nbt.setInteger("coords", tileEntityMachine.coords);
						nbt.setInteger("size", tileEntityMachine.meta);
						nbt.setString("roomname", tileEntityMachine.getCustomName());

						playerStack.setTagCompound(nbt);
					} else {
						if (tileEntityMachine.coords == -1) {
							player.addChatMessage(new ChatComponentTranslation("msg.message_machine_not_in_use.txt"));
						} else if (!tileEntityMachine.isUpgraded) {
							player.addChatMessage(new ChatComponentTranslation("msg.message_machine_not_upgraded.txt"));
						}
					}
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
