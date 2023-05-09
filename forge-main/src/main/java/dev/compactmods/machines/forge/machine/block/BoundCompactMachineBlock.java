package dev.compactmods.machines.forge.machine.block;

import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.machine.entity.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.forge.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.machine.item.ICompactMachineItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BoundCompactMachineBlock extends CompactMachineBlock implements EntityBlock {
    public BoundCompactMachineBlock(Properties props) {
        super(props);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return MachineBlockUtil.destroyProgress(state, player, level, pos);
    }

    @Override
    public void fillItemCategory(CreativeModeTab pTab, NonNullList<ItemStack> pItems) {
        // Do not add additional items to Creative
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            level.getBlockEntity(pos, Machines.MACHINE_ENTITY.get()).ifPresent(tile -> {
                // force client redraw
                final int color = ICompactMachineItem.getMachineColor(stack);
                tile.setColor(color);

                BoundCompactMachineItem.getRoom(stack).ifPresent(tile::setConnectedRoom);
            });
        }
    }

    @SuppressWarnings("deprecation")
    public void onRemove(BlockState oldState, Level level, BlockPos pos, BlockState newState, boolean a) {
        if (level.isClientSide) {
            super.onRemove(oldState, level, pos, newState, a);
            return;
        }

        MachineBlockUtil.cleanupTunnelsPostMachineRemove(level, pos);

        super.onRemove(oldState, level, pos, newState, a);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BoundCompactMachineBlockEntity(pos, state);
    }
}
