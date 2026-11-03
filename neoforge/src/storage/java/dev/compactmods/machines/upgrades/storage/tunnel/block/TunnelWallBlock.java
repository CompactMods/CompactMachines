package dev.compactmods.machines.upgrades.storage.tunnel.block;

import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.block.ProtectedWallBlock;
import dev.compactmods.machines.upgrades.storage.StorageCapabilities;
import dev.compactmods.machines.upgrades.storage.tunnel.Tunnels;
import dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigState;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;

/**
 * A room wall with a tunnel on it, bound to one of the room's named storages. Protected like the
 * solid wall it replaced.
 *
 * <p>Using it opens the tunnel config screen to edit the storage; sneak-using it with an empty hand
 * unbinds the storage (keeping its data), turns the wall back into a solid wall and pops the tunnel off.
 */
public class TunnelWallBlock extends ProtectedWallBlock implements EntityBlock {

    public TunnelWallBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TunnelWallBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (!(player instanceof ServerPlayer serverPlayer) || player instanceof FakePlayer
                || !(level instanceof ServerLevel serverLevel)
                || !(level.getBlockEntity(pos) instanceof TunnelWallBlockEntity tunnel))
            return InteractionResult.PASS;

        if (player.isShiftKeyDown() && player.getMainHandItem().isEmpty()) {
            removeTunnel(serverLevel, pos, tunnel);
            return InteractionResult.SUCCESS_SERVER;
        }

        final var editState = Tunnels.roomAt(level, pos)
                .map(room -> room.getCapability(StorageCapabilities.NAMED_STORAGES))
                .flatMap(storages -> storages.definition(tunnel.storageName())
                        .map(def -> new TunnelConfigState(def, storages.capacity(def))));

        // A wall whose storage is gone (e.g. its definition was dropped) can still be removed, not edited
        if (editState.isEmpty())
            return InteractionResult.PASS;

        Tunnels.openConfig(serverPlayer, pos, tunnel.face(), editState);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Unbinds the tunnel's storage, turns the wall back into a solid wall and drops the tunnel into the room. */
    private static void removeTunnel(ServerLevel level, BlockPos pos, TunnelWallBlockEntity tunnel) {
        final var face = tunnel.face();

        // Replacing the block unbinds the storage (see TunnelWallBlockEntity#preRemoveSideEffects)
        level.setBlock(pos, Rooms.Blocks.SOLID_WALL.get().defaultBlockState(), Block.UPDATE_ALL);
        level.invalidateCapabilities(pos);

        Block.popResource(level, pos.relative(face), new ItemStack(Tunnels.Items.TUNNEL.get()));
    }
}
