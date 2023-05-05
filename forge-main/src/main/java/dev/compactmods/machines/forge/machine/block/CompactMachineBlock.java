package dev.compactmods.machines.forge.machine.block;

import dev.compactmods.machines.api.shrinking.PSDTags;
import dev.compactmods.machines.forge.machine.entity.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.forge.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.forge.machine.item.UnboundCompactMachineItem;
import dev.compactmods.machines.room.BasicRoomInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * Primary block.
 *
 * @since 5.2.0
 */
public class CompactMachineBlock extends Block {

    CompactMachineBlock(Properties props) {
        super(props);
    }

    // client-side
    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof BoundCompactMachineBlockEntity be) {
            return be.connectedRoom().map(roomCode -> {
                final var roomInfo = new BasicRoomInfo(roomCode, be.getColor());
                return BoundCompactMachineItem.createForRoom(roomInfo);
            }).orElse(UnboundCompactMachineItem.unbound());
        }

        return UnboundCompactMachineItem.unbound();
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

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
