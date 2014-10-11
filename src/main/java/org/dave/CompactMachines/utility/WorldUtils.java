package org.dave.CompactMachines.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.init.ModBlocks;

public class WorldUtils {


	public static AxisAlignedBB getBoundingBoxForCube(int coord, int size) {
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
				coord * ConfigurationHandler.cubeDistance + 1, 40,  0,
				coord * ConfigurationHandler.cubeDistance + size+1, 40+size+1, size+1
		);

		return bb;
	}

	public static List<ItemStack> harvestCube(World worldObj, int posX1, int posY1, int posZ1, int posX2, int posY2, int posZ2) {
		int minX = Math.min(posX1, posX2);
		int minY = Math.min(posY1, posY2);
		int minZ = Math.min(posZ1, posZ2);

		int maxX = Math.max(posX1, posX2);
		int maxY = Math.max(posY1, posY2);
		int maxZ = Math.max(posZ1, posZ2);

		ArrayList<ItemStack> returnList = new ArrayList<ItemStack>();
		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					ArrayList<ItemStack> dropsList = getItemStackFromBlock(worldObj, x, y, z);
					if(dropsList != null) {
						for (ItemStack s : dropsList) {
							returnList.add(s);
						}
					}

					worldObj.setBlockToAir(x, y, z);

					// Collect any lost items laying around
					double[] head = new double[]{ x, y, z};
					AxisAlignedBB axis = AxisAlignedBB.getBoundingBox(head[0] - 2, head[1] - 2, head[2] - 2, head[0] + 3, head[1] + 3, head[2] + 3);
					List result = worldObj.getEntitiesWithinAABB(EntityItem.class, axis);
					for (int ii = 0; ii < result.size(); ii++) {
						if (result.get(ii) instanceof EntityItem) {
							EntityItem entity = (EntityItem) result.get(ii);
							if (entity.isDead) {
								continue;
							}

							ItemStack mineable = entity.getEntityItem();
							if (mineable.stackSize <= 0) {
								continue;
							}

							entity.worldObj.removeEntity(entity);
							returnList.add(mineable);
						}
					}
				}
			}
		}

		return returnList;
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

	public static HashMap<Integer, Vec3> generateCube(World worldObj, int posX1, int posY1, int posZ1, int posX2, int posY2, int posZ2)
	{
		int minX = Math.min(posX1, posX2);
		int minY = Math.min(posY1, posY2);
		int minZ = Math.min(posZ1, posZ2);

		int maxX = Math.max(posX1, posX2);
		int maxY = Math.max(posY1, posY2);
		int maxZ = Math.max(posZ1, posZ2);

		int midX = (int)Math.floor((posX1 + posX2) / 2);
		int midY = (int)Math.floor((posY1 + posY2) / 2);
		int midZ = (int)Math.floor((posZ1 + posZ2) / 2);

		HashMap<Integer, Vec3> interfaces = new HashMap<Integer, Vec3>();

		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					if (x == minX || y == minY || z == minZ || x == maxX || y == maxY || z == maxZ)
					{
						Vec3 pos = Vec3.createVectorHelper(x, y, z);
						if(x == midX && y == midY && z == minZ) {
							// XY mid, Z min --> north
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
							interfaces.put(ForgeDirection.NORTH.ordinal(), pos);
						} else if(x == midX && y == midY && z == maxZ) {
							// XY mid, Z max --> south
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
							interfaces.put(ForgeDirection.SOUTH.ordinal(), pos);
						} else if(x == midX && y == minY && z == midZ) {
							// XZ mid, Y min --> down
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
							interfaces.put(ForgeDirection.DOWN.ordinal(), pos);
						} else if(x == midX && y == maxY && z == midZ) {
							// XZ mid, Y max --> up
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
							interfaces.put(ForgeDirection.UP.ordinal(), pos);
						} else if(x == minX && y == midY && z == midZ) {
							// YZ mid, X min --> west
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
							interfaces.put(ForgeDirection.WEST.ordinal(), pos);
						} else if(x == maxX && y == midY && z == midZ) {
							// YZ mid, X max --> east
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
							interfaces.put(ForgeDirection.EAST.ordinal(), pos);
						} else {
							worldObj.setBlock(x, y, z, ModBlocks.innerwall, 0, 2);
						}
					}
				}
			}
		}

		return interfaces;
	}
}

