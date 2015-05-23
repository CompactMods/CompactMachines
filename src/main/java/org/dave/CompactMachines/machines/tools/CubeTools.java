package org.dave.CompactMachines.machines.tools;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.util.ForgeDirection;

import org.dave.CompactMachines.CompactMachines;
import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.init.ModBlocks;
import org.dave.CompactMachines.integration.item.ItemSharedStorage;
import org.dave.CompactMachines.reference.Reference;
import org.dave.CompactMachines.tileentity.TileEntityInterface;
import org.dave.CompactMachines.tileentity.TileEntityMachine;
import org.dave.CompactMachines.utility.WorldUtils;

public class CubeTools {

	public static int getCoordByPos(double x) {
		return (int) (x / ConfigurationHandler.cubeDistance);
	}

	public static int getCubeSize(IBlockAccess world, int coord) {
		int base = coord * ConfigurationHandler.cubeDistance;
		if(world.getBlock(base+14, 40, 0) == ModBlocks.innerwall) {
			return 5;
		}

		if(world.getBlock(base+12, 40, 0) == ModBlocks.innerwall) {
			return 4;
		}

		if(world.getBlock(base+10, 40, 0) == ModBlocks.innerwall) {
			return 3;
		}

		if(world.getBlock(base+8, 40, 0) == ModBlocks.innerwall) {
			return 2;
		}

		if(world.getBlock(base+6, 40, 0) == ModBlocks.innerwall) {
			return 1;
		}

		return 0;
	}

	public static boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		int coord = CubeTools.getCoordByPos(x);
		int size = Reference.getBoxSize(CubeTools.getCubeSize(world, coord));

		int relativeX = x - (coord * ConfigurationHandler.cubeDistance);

		// Bottom layer
		if(y == 41 && relativeX > 0 && relativeX < size && z < size && z > 0) {
			if(side == ForgeDirection.UP.ordinal()) {
				return true;
			}
		}

		if(y == 39+size && relativeX > 0 && relativeX < size && z < size && z > 0) {
			if(side == ForgeDirection.DOWN.ordinal()) {
				return true;
			}
		}

		if(y > 40 && y < 40+size) {
			if(side == ForgeDirection.EAST.ordinal() && relativeX == 1 && z < size && z > 0) {
				return true;
			}

			if(side == ForgeDirection.WEST.ordinal() && relativeX == size-1 && z < size && z > 0) {
				return true;
			}

			if(side == ForgeDirection.NORTH.ordinal() && z == size-1 && relativeX < size && relativeX > 0) {
				return true;
			}

			if(side == ForgeDirection.SOUTH.ordinal() && z == 1 && relativeX < size && relativeX > 0) {
				return true;
			}

		}

		return false;
	}

	public static void setCubeBiome(int coords, BiomeGenBase biome) {
		WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);
		Chunk chunk = machineWorld.getChunkFromBlockCoords(coords * ConfigurationHandler.cubeDistance, 0);
		if(chunk != null && chunk.isChunkLoaded) {
			byte[] biomeArray = chunk.getBiomeArray();
			for(int x = 0; x < 15; x++) {
				for(int z = 0; z < 15; z++) {
					biomeArray[z << 4 | x] = (byte) biome.biomeID;
				}
			}
		}
	}

	public static BiomeGenBase getMachineBiome(TileEntityMachine machine) {
		byte biomeArray[] = machine.getWorldObj().getChunkFromBlockCoords(machine.xCoord, machine.zCoord).getBiomeArray();
		int biomeId = biomeArray[((machine.zCoord & 0xF) << 4 | machine.xCoord & 0xF)];

		if(biomeId > 0 && biomeId < BiomeGenBase.getBiomeGenArray().length && BiomeDictionary.isBiomeRegistered(biomeId)) {
			return BiomeGenBase.getBiome(biomeId);
		}

		return WorldUtils.getBiomeByName(ConfigurationHandler.defaultBiome);
	}

	public static void generateCube(TileEntityMachine machine) {
		int size = Reference.getBoxSize(machine.meta);
		int height = size;

		WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);

		generateCube(machineWorld,
				//          x           y           z
				machine.coords * ConfigurationHandler.cubeDistance, 40, 0,
				machine.coords * ConfigurationHandler.cubeDistance + size, 40 + height, size
				);

		if(ConfigurationHandler.adaptBiomes) {
			setCubeBiome(machine.coords, getMachineBiome(machine));
		} else {
			BiomeGenBase biome = WorldUtils.getBiomeByName(ConfigurationHandler.defaultBiome);
			setCubeBiome(machine.coords, biome);
		}

		// After creating the Block, make sure the TileEntities inside have their information ready.
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			Vec3 pos = CubeTools.getInterfacePosition(machine.coords, machine.meta, dir);
			TileEntityInterface te = (TileEntityInterface) machineWorld.getTileEntity((int) pos.xCoord, (int) pos.yCoord, (int) pos.zCoord);
			te.setCoordSide(machine.coords, dir.ordinal());
		}
	}

	private static void generateCube(World worldObj, int posX1, int posY1, int posZ1, int posX2, int posY2, int posZ2)
	{
		int minX = Math.min(posX1, posX2);
		int minY = Math.min(posY1, posY2);
		int minZ = Math.min(posZ1, posZ2);

		int maxX = Math.max(posX1, posX2);
		int maxY = Math.max(posY1, posY2);
		int maxZ = Math.max(posZ1, posZ2);

		int midX = (int) Math.floor((posX1 + posX2) / 2);
		int midY = (int) Math.floor((posY1 + posY2) / 2);
		int midZ = (int) Math.floor((posZ1 + posZ2) / 2);

		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					if (x == minX || y == minY || z == minZ || x == maxX || y == maxY || z == maxZ)
					{
						Vec3 pos = Vec3.createVectorHelper(x, y, z);
						if (x == midX && y == midY && z == minZ) {
							// XY mid, Z min --> north
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
						} else if (x == midX && y == midY && z == maxZ) {
							// XY mid, Z max --> south
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
						} else if (x == midX && y == minY && z == midZ) {
							// XZ mid, Y min --> down
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
						} else if (x == midX && y == maxY && z == midZ) {
							// XZ mid, Y max --> up
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
						} else if (x == minX && y == midY && z == midZ) {
							// YZ mid, X min --> west
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
						} else if (x == maxX && y == midY && z == midZ) {
							// YZ mid, X max --> east
							worldObj.setBlock(x, y, z, ModBlocks.interfaceblock, 0, 2);
						} else {
							worldObj.setBlock(x, y, z, ModBlocks.innerwall, 0, 2);
						}
					}
				}
			}
		}
	}

	public static void harvestMachine(TileEntityMachine machine, EntityPlayer player) {
		if (machine.coords == -1) {
			return;
		}

		WorldServer machineWorld = MinecraftServer.getServer().worldServerForDimension(ConfigurationHandler.dimensionId);

		int size = Reference.getBoxSize(machine.meta);
		int height = size;

		List<ItemStack> stacks = harvestCube(machineWorld,
				//   x           y           z
				machine.coords * ConfigurationHandler.cubeDistance + 1, 40 + 1, 1,
				machine.coords * ConfigurationHandler.cubeDistance + size - 1, 40 + height - 1, size - 1,
				player
				);

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
			ItemSharedStorage storage = machine.getStorage(dir.ordinal());
			ItemStack storedStack = storage.getStackInSlot(0);
			if (storedStack != null && storedStack.stackSize > 0) {
				stacks.add(storedStack);
			}
			storage.setInventorySlotContents(0, null);
			storage.setDirty();
		}

		World worldObj = CompactMachines.instance.machineHandler.getWorld();
		int droppedStacks = 0;
		for (ItemStack stack : stacks) {
			if (ConfigurationHandler.maxDroppedStacks != -1 && droppedStacks >= ConfigurationHandler.maxDroppedStacks) {
				return;
			}

			EntityItem entityitem = new EntityItem(machine.getWorldObj(), machine.xCoord, machine.yCoord + 0.5F, machine.zCoord, stack);

			entityitem.lifespan = 1200;
			entityitem.delayBeforeCanPickup = 10;

			float f3 = 0.05F;
			entityitem.motionX = (float) worldObj.rand.nextGaussian() * f3;
			entityitem.motionY = (float) worldObj.rand.nextGaussian() * f3 + 0.2F;
			entityitem.motionZ = (float) worldObj.rand.nextGaussian() * f3;
			machine.getWorldObj().spawnEntityInWorld(entityitem);
			droppedStacks++;
		}

		return;
	}

	private static List<ItemStack> harvestCube(World worldObj, int posX1, int posY1, int posZ1, int posX2, int posY2, int posZ2, EntityPlayer player) {
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
					ArrayList<ItemStack> dropsList = WorldUtils.getItemStackFromBlock(worldObj, x, y, z);
					if (dropsList != null) {
						for (ItemStack s : dropsList) {
							returnList.add(s);
						}
					}

					if (player != null && worldObj.getTileEntity(x, y, z) instanceof TileEntityMachine) {
						Block block = worldObj.getBlock(x, y, z);
						block.removedByPlayer(worldObj, player, x, y, z, true);
					}

					worldObj.setBlockToAir(x, y, z);

					// Collect any lost items laying around
					double[] head = new double[] { x, y, z };
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

	public static Vec3 getInterfacePosition(int coord, int meta, ForgeDirection dir) {
		int size = Reference.getBoxSize(meta);
		int height = size;

		int xMin = coord * ConfigurationHandler.cubeDistance;
		int yMin = 40;
		int zMin = 0;

		int midX = xMin + (size / 2);
		int midY = yMin + (size / 2);
		int midZ = zMin + (size / 2);

		int x = 0;
		int y = 0;
		int z = 0;

		if(dir == ForgeDirection.DOWN) {
			y = yMin;
			x = midX;
			z = midZ;
		} else if(dir == ForgeDirection.UP) {
			y = yMin + size;
			x = midX;
			z = midZ;
		} else {
			y = midY;

			if(dir == ForgeDirection.NORTH) {
				// XY mid, Z min --> north
				x = midX;
				z = zMin;
			} else if(dir == ForgeDirection.SOUTH) {
				x = midX;
				z = zMin + size;
			} else if(dir == ForgeDirection.EAST) {
				// YZ mid, X max --> east
				z = midZ;
				x = xMin + size;
			} else if(dir == ForgeDirection.WEST) {
				z = midZ;
				x = xMin;
			}

		}

		return Vec3.createVectorHelper(x, y, z);
	}

	public static AxisAlignedBB getBoundingBoxForCube(int coord, int size) {
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
				coord * ConfigurationHandler.cubeDistance + 1, 40, 0,
				coord * ConfigurationHandler.cubeDistance + size + 1, 40 + size + 1, size + 1
				);

		return bb;
	}

}
