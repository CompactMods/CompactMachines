package dev.compactmods.machines.block.walls;

import javax.annotation.Nullable;
import java.util.Optional;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.redstone.IRedstoneReaderTunnel;
import dev.compactmods.machines.block.tiles.TunnelWallEntity;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import dev.compactmods.machines.tunnel.TunnelHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class TunnelWallBlock extends ProtectedWallBlock implements EntityBlock {
    public static DirectionProperty TUNNEL_SIDE = DirectionProperty.create("tunnel_side", Direction.values());
    public static DirectionProperty CONNECTED_SIDE = DirectionProperty.create("connected_side", Direction.values());

    public static BooleanProperty REDSTONE = BooleanProperty.create("redstone");

    public TunnelWallBlock(Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any()
                .setValue(CONNECTED_SIDE, Direction.UP)
                .setValue(TUNNEL_SIDE, Direction.UP)
                .setValue(REDSTONE, false)
        );
    }

    public Optional<TunnelDefinition> getTunnelInfo(BlockGetter world, BlockPos position) {
        TunnelWallEntity tile = (TunnelWallEntity) world.getBlockEntity(position);
        if (tile == null)
            return Optional.empty();

        return tile.getTunnelDefinition();
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos position, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return false;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter world, BlockPos position, Direction side) {
        return 0;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter world, BlockPos position, Direction side) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(world, position);
        if (!tunnelInfo.isPresent())
            return 0;

        TunnelDefinition definition = tunnelInfo.get();
        if (definition instanceof IRedstoneReaderTunnel) {
            // TODO - Redstone tunnels
            // ITunnelConnectionInfo conn = TunnelHelper.generateConnectionInfo(world, position);
            // int weak = ((IRedstoneReaderTunnel) definition).getPowerLevel(conn);
            // return weak;
            return 0;
        }

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
        if (level.isClientSide())
            return InteractionResult.SUCCESS;

        if (player.isShiftKeyDown()) {
            Optional<TunnelDefinition> tunnelDef = getTunnelInfo(level, pos);

            if (!tunnelDef.isPresent())
                return InteractionResult.FAIL;

            BlockState solidWall = Registration.BLOCK_SOLID_WALL.get().defaultBlockState();

            level.setBlockAndUpdate(pos, solidWall);

            TunnelDefinition tunnelRegistration = tunnelDef.get();
            ItemStack stack = new ItemStack(Tunnels.ITEM_TUNNEL.get(), 1);
            CompoundTag defTag = stack.getOrCreateTagElement("definition");
            defTag.putString("id", tunnelRegistration.getRegistryName().toString());

            ItemEntity ie = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
            level.addFreshEntity(ie);
        } else {
            // Rotate tunnel
            Direction dir = state.getValue(CONNECTED_SIDE);
            Direction nextDir = TunnelHelper.getNextDirection(dir);

            level.setBlockAndUpdate(pos, state.setValue(CONNECTED_SIDE, nextDir));
        }

        return InteractionResult.SUCCESS;
    }
}
