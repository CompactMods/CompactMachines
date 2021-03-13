package com.robotgryphon.compactmachines.block.walls;

import com.robotgryphon.compactmachines.api.tunnels.ITunnelConnectionInfo;
import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.compat.theoneprobe.IProbeData;
import com.robotgryphon.compactmachines.compat.theoneprobe.IProbeDataProvider;
import com.robotgryphon.compactmachines.compat.theoneprobe.providers.TunnelProvider;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.api.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.tunnels.TunnelHelper;
import com.robotgryphon.compactmachines.api.tunnels.redstone.IRedstoneReaderTunnel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

import net.minecraft.block.AbstractBlock.Properties;

public class TunnelWallBlock extends ProtectedWallBlock implements IProbeDataProvider {
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

    public Optional<TunnelDefinition> getTunnelInfo(IBlockReader world, BlockPos pos) {
        TunnelWallTile tile = (TunnelWallTile) world.getBlockEntity(pos);
        if (tile == null)
            return Optional.empty();

        return tile.getTunnelDefinition();
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(world, pos);
        if (!tunnelInfo.isPresent())
            return false;

        TunnelDefinition definition = tunnelInfo.get();
        if (definition instanceof IRedstoneReaderTunnel) {
            ITunnelConnectionInfo conn = TunnelHelper.generateConnectionInfo(world, pos);
            return ((IRedstoneReaderTunnel) definition).canConnectRedstone(conn);
        }

        return false;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return state.getValue(REDSTONE);
    }

    @Override
    public int getDirectSignal(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        return 0;
    }

    @Override
    public int getSignal(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(world, pos);
        if (!tunnelInfo.isPresent())
            return 0;

        TunnelDefinition definition = tunnelInfo.get();
        if (definition instanceof IRedstoneReaderTunnel) {
            ITunnelConnectionInfo conn = TunnelHelper.generateConnectionInfo(world, pos);
            int weak = ((IRedstoneReaderTunnel) definition).getPowerLevel(conn);
            return weak;
        }

        return 0;
    }

    @Override
    public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isClientSide())
            return ActionResultType.SUCCESS;


        if (player.isShiftKeyDown()) {
            Optional<TunnelDefinition> tunnelDef = getTunnelInfo(worldIn, pos);

            if (!tunnelDef.isPresent())
                return ActionResultType.FAIL;

            BlockState solidWall = Registration.BLOCK_SOLID_WALL.get().defaultBlockState();

            worldIn.setBlockAndUpdate(pos, solidWall);

            TunnelDefinition tunnelRegistration = tunnelDef.get();
            ItemStack stack = new ItemStack(Registration.ITEM_TUNNEL.get(), 1);
            CompoundNBT defTag = stack.getOrCreateTagElement("definition");
            defTag.putString("id", tunnelRegistration.getRegistryName().toString());

            ItemEntity ie = new ItemEntity(worldIn, player.getX(), player.getY(), player.getZ(), stack);
            worldIn.addFreshEntity(ie);

//                        IFormattableTextComponent t = new StringTextComponent(tunnelRegistration.getRegistryName().toString())
//                                .mergeStyle(TextFormatting.GRAY);
//
//                        player.sendStatusMessage(t, true);
        } else {
            // Rotate tunnel
            Direction dir = state.getValue(CONNECTED_SIDE);
            Direction nextDir = TunnelHelper.getNextDirection(dir);

            worldIn.setBlockAndUpdate(pos, state.setValue(CONNECTED_SIDE, nextDir));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(TUNNEL_SIDE).add(CONNECTED_SIDE).add(REDSTONE);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TunnelWallTile();
    }

    @Override
    public void addProbeData(IProbeData data, PlayerEntity player, World world, BlockState state) {
        TunnelProvider.exec(data, player, world, state);
    }
}
