package org.dave.compactmachines3.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.dave.compactmachines3.init.Itemss;

public abstract class BlockProtected extends BlockBase {
    public BlockProtected(Material material) {
        super(material);

        this.setBlockUnbreakable();
        this.setResistance(6000000.0F);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if(!isBlockProtected(state, world, pos)) {
            return super.removedByPlayer(state, world, pos, player, willHarvest);
        }

        if(!world.isRemote && player instanceof EntityPlayerMP && player.capabilities.isCreativeMode) {
            ItemStack playerStack = player.getHeldItemMainhand();

            if(!playerStack.isEmpty() && playerStack.getItem() == Itemss.miniFluidDrop) {
                return super.removedByPlayer(state, world, pos, player, willHarvest);
            }
        }

        return false;
    }

    public boolean isBlockProtected(IBlockState state, IBlockAccess world, BlockPos pos) {
        return true;
    };

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
        if(isBlockProtected(state, world, pos)) {
            return false;
        }

        return super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
        if(isBlockProtected(state, world, pos)) {
            return false;
        }

        return super.canBeReplacedByLeaves(state, world, pos);
    }

    @Override
    public boolean canDropFromExplosion(Explosion explosionIn) {
        return true;
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        if(isBlockProtected(world.getBlockState(pos), world, pos)) {
            return;
        }

        super.onBlockExploded(world, pos, explosion);
    }
}
