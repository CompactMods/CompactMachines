package org.dave.compactmachines3.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class BlockProtected extends Block {
    public BlockProtected(Properties properties) {
        super(properties);
    }

//    @Override
//    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest) {

//    }


    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
        if(!isBlockProtected(state, world, pos)) {
            return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
        }

        if(!world.isRemote && player instanceof ServerPlayerEntity && player.isCreative()) {
            ItemStack playerStack = player.getHeldItemMainhand();

            // TODO: Personal Shrinking Device in Creative Mode
            return false;
//            if(!playerStack.isEmpty() && playerStack.getItem() == Itemss.psd) {
//                return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
//            }
        }

        return false;
    }

    public boolean isBlockProtected(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }

    @Override
    public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
        if(isBlockProtected(world.getBlockState(pos), world, pos)) {
            return;
        }

        super.onBlockExploded(state, world, pos, explosion);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, IBlockReader world, BlockPos pos, Entity entity) {
        if(isBlockProtected(state, world, pos))
            return false;

        return super.canEntityDestroy(state, world, pos, entity);
    }

    @Override
    public boolean canBeReplacedByLeaves(BlockState state, IWorldReader world, BlockPos pos) {
        if(isBlockProtected(state, world, pos))
            return false;

        return super.canBeReplacedByLeaves(state, world, pos);
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return true;
    }
}
