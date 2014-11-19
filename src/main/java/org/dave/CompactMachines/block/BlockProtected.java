package org.dave.CompactMachines.block;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import org.dave.CompactMachines.item.ItemAtomShrinker;

public class BlockProtected extends BlockCM {
	public BlockProtected()
	{
		super();
		this.setBlockUnbreakable();
		this.setResistance(6000000.0F);
	}

	// Prevent players from breaking the block except when they carry an Atom Shrinker and
	// are in creative mode. Why did I make this so awkward?
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z) {
		if (!world.isRemote && player instanceof EntityPlayerMP && player.capabilities.isCreativeMode) {
			ItemStack playerStack = player.getCurrentEquippedItem();
			if (playerStack != null && playerStack.getItem() instanceof ItemAtomShrinker) {
				return super.removedByPlayer(world, player, x, y, z);
			}
		}

		return false;
	}

	// Prevent blocks from being placed by players
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		world.setBlockToAir(x, y, z);
		return;
	}

	// Prevent entities from destroying the block
	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity) {
		return false;
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
		return false;
	}

	// Well, why not...
	@Override
	public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z) {
		return false;
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		return;
	}

}
