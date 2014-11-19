package org.dave.CompactMachines.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidContainerRegistry;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.reference.GuiId;
import org.dave.CompactMachines.reference.Names;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.utility.FluidUtils;

public class BlockInterface extends BlockProtected implements ITileEntityProvider {

	public BlockInterface()
	{
		super();
		this.setBlockName(Names.Blocks.INTERFACE);
		this.setBlockTextureName(Names.Blocks.INTERFACE);
		this.setLightOpacity(1);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityInterface();
	}

	@Override
	public boolean hasTileEntity(int metadata) {
		return true;
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		return false;
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
			if (!(world.getTileEntity(x, y, z) instanceof TileEntityInterface)) {
				return false;
			}

			TileEntityInterface tank = (TileEntityInterface) world.getTileEntity(x, y, z);

			ItemStack playerStack = player.inventory.getCurrentItem();

			// XXX: Do we need to do anything for gases here?

			if (playerStack != null) {
				if (FluidContainerRegistry.isEmptyContainer(playerStack)) {
					FluidUtils.emptyTankIntoContainer(tank, player, tank.getFluid());
					world.markBlockForUpdate(x, y, z);
					return true;
				} else if (FluidContainerRegistry.isFilledContainer(playerStack)) {
					FluidUtils.fillTankWithContainer(tank, player);
					world.markBlockForUpdate(x, y, z);
					return true;
				}
			}

			if (!world.isRemote && player instanceof EntityPlayerMP)
			{
				player.openGui(CompactMachines.instance, GuiId.INTERFACE.ordinal(), world, x, y, z);
			}

			return true;
		}
	}
}
