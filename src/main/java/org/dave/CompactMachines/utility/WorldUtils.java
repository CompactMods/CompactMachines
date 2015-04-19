package org.dave.CompactMachines.utility;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.reference.Reference;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;

public class WorldUtils {

	public static int updateNeighborAEGrids(World world, int x, int y, int z) {
		if (!Reference.AE_AVAILABLE) {
			return 0;
		}

		int countUpdated = 0;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			int offX = x + dir.offsetX;
			int offY = y + dir.offsetY;
			int offZ = z + dir.offsetZ;

			if (world.getTileEntity(offX, offY, offZ) instanceof IGridHost) {
				IGridHost host = (IGridHost) world.getTileEntity(offX, offY, offZ);
				IGridNode node = host.getGridNode(dir.getOpposite());

				if (node == null) {
					node = host.getGridNode(ForgeDirection.UNKNOWN);
				}

				if (node != null) {
					node.updateState();
					//LogHelper.info("Updating node state on side: " + dir);
					countUpdated++;
				}
			}
		}

		return countUpdated;
	}

	public static ArrayList<ItemStack> getItemStackFromBlock(World world, int i, int j, int k) {
		Block block = world.getBlock(i, j, k);

		if (block == null) {
			return null;
		}

		if (block.isAir(world, i, j, k)) {
			return null;
		}

		int meta = world.getBlockMetadata(i, j, k);

		return block.getDrops(world, i, j, k, meta, 0);
	}
}
