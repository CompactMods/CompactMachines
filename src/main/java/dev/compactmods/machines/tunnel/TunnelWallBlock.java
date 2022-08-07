package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelTeardownHandler;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.wall.ProtectedWallBlock;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class TunnelWallBlock extends ProtectedWallBlock implements EntityBlock {
    public static final DirectionProperty TUNNEL_SIDE = DirectionProperty.create("tunnel_side", Direction.values());
    public static final DirectionProperty CONNECTED_SIDE = DirectionProperty.create("connected_side", Direction.values());

    public static final BooleanProperty REDSTONE = BooleanProperty.create("redstone");

    public TunnelWallBlock(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any()
                .setValue(CONNECTED_SIDE, Direction.UP)
                .setValue(TUNNEL_SIDE, Direction.UP)
                .setValue(REDSTONE, false)
        );
    }

    public static Optional<TunnelDefinition> getTunnelInfo(BlockGetter world, BlockPos position) {
        TunnelWallEntity tile = (TunnelWallEntity) world.getBlockEntity(position);
        if (tile == null)
            return Optional.empty();

        return Optional.ofNullable(tile.getTunnelType());
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos position, @Nullable Direction side) {
        return state.getValue(REDSTONE);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(REDSTONE);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos position, Direction side) {
        return 0;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos position, Direction side) {
        // TODO - Redstone tunnels
        // Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(world, position);
        return 0;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TUNNEL_SIDE).add(CONNECTED_SIDE).add(REDSTONE);
        super.createBlockStateDefinition(builder);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TunnelWallEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide)
            return InteractionResult.SUCCESS;

        if (!(level.getBlockEntity(pos) instanceof TunnelWallEntity tunnel))
            return InteractionResult.FAIL;

        if(level.dimension().equals(CompactDimension.LEVEL_KEY) && level instanceof ServerLevel compactDim) {
            var def = tunnel.getTunnelType();
            final Direction tunnelWallSide = hitResult.getDirection();

            if (player.isShiftKeyDown()) {
                BlockState solidWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();

                level.setBlockAndUpdate(pos, solidWall);

                ItemStack stack = new ItemStack(Tunnels.ITEM_TUNNEL.get(), 1);
                CompoundTag defTag = stack.getOrCreateTagElement("definition");
                defTag.putString("id", Tunnels.getRegistryId(def).toString());

                ItemEntity ie = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
                level.addFreshEntity(ie);

                if (def instanceof TunnelTeardownHandler teardown) {
                    teardown.onRemoved(new TunnelPosition(compactDim, pos, tunnelWallSide), tunnel.getTunnel());
                }

                final var tunnels = TunnelConnectionGraph.forRoom(compactDim, new ChunkPos(pos));
                tunnels.unregister(pos);
            } else {
                // Rotate tunnel
                Direction dir = state.getValue(CONNECTED_SIDE);

                final var tunnelGraph = TunnelConnectionGraph.forRoom(compactDim, new ChunkPos(pos));
                final var existingDirs = tunnelGraph
                        .getTunnelSides(def)
                        .collect(Collectors.toSet());

                if (existingDirs.size() == 6) {
                    // WARN PLAYER - NO OTHER SIDES REMAIN
                    player.displayClientMessage(
                            TranslationUtil.message(Messages.NO_TUNNEL_SIDE).withStyle(ChatFormatting.DARK_RED), true);

                    return InteractionResult.FAIL;
                }

                final var next = TunnelHelper.getNextDirection(dir, existingDirs);
                next.ifPresent(newSide -> {
                    level.setBlockAndUpdate(pos, state.setValue(CONNECTED_SIDE, newSide));

                    if (def instanceof TunnelTeardownHandler teardown) {
                        teardown.onRotated(new TunnelPosition(compactDim, pos, tunnelWallSide), tunnel.getTunnel(), dir, newSide);
                    }

                    tunnelGraph.rotateTunnel(pos, newSide);
                    tunnelGraph.setDirty();
                });
            }
        }

        return InteractionResult.SUCCESS;
    }

    // todo - breaking block unregisters tunnel info
}
