package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.tunnels.TunnelPosition;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelTeardownHandler;
import dev.compactmods.machines.api.tunnels.redstone.RedstoneReaderTunnel;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class TunnelWallBlock extends ProtectedWallBlock implements EntityBlock {
    public static final DirectionProperty TUNNEL_SIDE = DirectionProperty.create("tunnel_side", Direction.values());
    public static final DirectionProperty CONNECTED_SIDE = DirectionProperty.create("connected_side", Direction.values());

    public static final BooleanProperty REDSTONE = BooleanProperty.create("redstone");

    public TunnelWallBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any()
                .setValue(CONNECTED_SIDE, Direction.UP)
                .setValue(TUNNEL_SIDE, Direction.UP)
                .setValue(REDSTONE, false)
        );
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
        if (!state.getValue(REDSTONE)) return 0;
        if (world instanceof ServerLevel sl && world.getBlockEntity(position) instanceof TunnelWallEntity tunnelWall) {
            final var serv = sl.getServer();
            final var def = tunnelWall.getTunnelType();
            final var machPos = tunnelWall.getConnectedPosition();
            final var tunnPos = tunnelWall.getTunnelPosition();

            if (!machPos.isLoaded(serv)) return 0;

            if (def instanceof RedstoneReaderTunnel rrt) {
                return rrt.powerLevel(serv, machPos, tunnPos);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
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

        if (level.dimension().equals(CompactDimension.LEVEL_KEY) && level instanceof ServerLevel compactDim) {
            var def = tunnel.getTunnelType();
            final Direction tunnelWallSide = hitResult.getDirection();
            var tunnelConnectedSide = tunnel.getConnectedSide();

            final var tunnelId = Tunnels.getRegistryId(def);

            final var roomProvider = CompactRoomProvider.instance(compactDim);
            return roomProvider.findByChunk(new ChunkPos(pos)).map(roomInfo -> {
                final var tunnels = TunnelConnectionGraph.forRoom(compactDim, roomInfo.code());
                if (player.isShiftKeyDown()) {
                    BlockState solidWall = Walls.BLOCK_SOLID_WALL.get().defaultBlockState();

                    level.setBlockAndUpdate(pos, solidWall);

                    ItemStack stack = new ItemStack(Tunnels.ITEM_TUNNEL.get(), 1);
                    CompoundTag defTag = stack.getOrCreateTagElement("definition");
                    defTag.putString("id", tunnelId.toString());

                    ItemEntity ie = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
                    level.addFreshEntity(ie);

                    if (def instanceof TunnelTeardownHandler<?> teardown) {
                        teardown.onRemoved(compactDim.getServer(), new TunnelPosition(pos, tunnelWallSide, tunnelConnectedSide), tunnel.getTunnel());
                    }

                    tunnels.unregister(pos);
                } else {
                    // Rotate tunnel
                    Direction dir = state.getValue(CONNECTED_SIDE);

                    final var existingDirs = tunnels
                            .getTunnelSides(tunnelId)
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

                        if (def instanceof TunnelTeardownHandler<?> teardown) {
                            teardown.onRotated(compactDim.getServer(), new TunnelPosition(pos, tunnelWallSide, tunnelConnectedSide), tunnel.getTunnel(), dir, newSide);
                        }

                        tunnels.rotateTunnel(pos, newSide);
                        tunnels.setDirty();
                    });
                }

                return InteractionResult.SUCCESS;
            }).orElseGet(() -> {
                CompactMachines.LOGGER.fatal("Failed to interact with tunnel: not assigned to a room");
                return InteractionResult.FAIL;
            });
        }

        return InteractionResult.SUCCESS;
    }
    // todo - breaking block unregisters tunnel info
}
