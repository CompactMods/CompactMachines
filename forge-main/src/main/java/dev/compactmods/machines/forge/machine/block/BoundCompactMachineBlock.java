package dev.compactmods.machines.forge.machine.block;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.machine.entity.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.forge.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.forge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.machine.item.ICompactMachineItem;
import dev.compactmods.machines.room.BasicRoomInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BoundCompactMachineBlock extends CompactMachineBlock implements EntityBlock {
    public BoundCompactMachineBlock(Properties props) {
        super(props);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity be) {
            return be.connectedRoom().map(roomCode -> {
                final var roomInfo = new BasicRoomInfo(roomCode, be.getColor());
                return BoundCompactMachineItem.createForRoom(roomInfo);
            }).orElse(UnboundCompactMachineItem.unbound());
        }

        LoggingUtil.modLog().warn("Warning: tried to pick block on a machine that does not have an associated block entity.");
        return null;
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

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        MinecraftServer server = level.getServer();
        ItemStack mainItem = player.getMainHandItem();
        if (mainItem.is(PSDTags.ITEM) && player instanceof ServerPlayer sp) {
            return MachineBlockUtil.tryRoomTeleport(level, pos, sp, server);
        }

        // All other items, open preview screen
        if(!level.isClientSide) {
            level.getBlockEntity(pos, Machines.MACHINE_ENTITY.get()).ifPresent(machine -> {
                MachineBlockUtil.roomPreviewScreen(pos, (ServerPlayer) player, server, machine);
            });
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
