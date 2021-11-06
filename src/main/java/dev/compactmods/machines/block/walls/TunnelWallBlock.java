package dev.compactmods.machines.block.walls;

import dev.compactmods.machines.api.tunnels.ITunnelConnectionInfo;
import dev.compactmods.machines.block.tiles.TunnelWallTile;
import dev.compactmods.machines.compat.theoneprobe.IProbeData;
import dev.compactmods.machines.compat.theoneprobe.IProbeDataProvider;
import dev.compactmods.machines.compat.theoneprobe.providers.TunnelProvider;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.tunnels.TunnelHelper;
import dev.compactmods.machines.api.tunnels.redstone.IRedstoneReaderTunnel;
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
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Optional;

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
        return false;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return false;
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
