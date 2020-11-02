package com.robotgryphon.compactmachines.block.walls;

import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.tunnels.EnumTunnelSide;
import com.robotgryphon.compactmachines.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.tunnels.TunnelHelper;
import com.robotgryphon.compactmachines.tunnels.api.IRedstoneTunnel;
import mcjty.theoneprobe.api.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public class TunnelWallBlock extends WallBlock implements IProbeInfoAccessor {
    public static DirectionProperty TUNNEL_SIDE = DirectionProperty.create("tunnel_side", Direction.values());
    public static BooleanProperty REDSTONE = BooleanProperty.create("redstone");

    public TunnelWallBlock(Properties props) {
        super(props);
        setDefaultState(getStateContainer().getBaseState()
                .with(TUNNEL_SIDE, Direction.UP)
                .with(REDSTONE, false)
        );
    }

    public Optional<TunnelDefinition> getTunnelInfo(IBlockReader world, BlockPos pos) {
        TunnelWallTile tile = (TunnelWallTile) world.getTileEntity(pos);
        if(tile == null)
            return Optional.empty();

        return tile.getTunnelDefinition();
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(world, pos);
        if (!tunnelInfo.isPresent())
            return false;

        TunnelDefinition definition = tunnelInfo.get();
        if (definition instanceof IRedstoneTunnel) {
            return ((IRedstoneTunnel) definition).canConnectRedstone(world, state, pos, side);
        }

        return false;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return state.get(REDSTONE);
    }

    @Override
    public int getStrongPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(world, pos);
        if (!tunnelInfo.isPresent())
            return 0;

        TunnelDefinition definition = tunnelInfo.get();
        if (definition instanceof IRedstoneTunnel) {
            return ((IRedstoneTunnel) definition).getStrongPower(world, state, pos, side);
        }

        return 0;
    }

    @Override
    public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(world, pos);
        if (!tunnelInfo.isPresent())
            return 0;

        TunnelDefinition definition = tunnelInfo.get();
        if (definition instanceof IRedstoneTunnel) {
            return ((IRedstoneTunnel) definition).getWeakPower(world, state, pos, side);
        }

        return 0;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote())
            return ActionResultType.SUCCESS;


        if (player.isSneaking()) {
            // TODO Remove tunnelDef and return
            Optional<TunnelDefinition> tunnelDef = getTunnelInfo(worldIn, pos);

            if (!tunnelDef.isPresent())
                return ActionResultType.FAIL;

            BlockState solidWall = Registrations.BLOCK_SOLID_WALL.get().getDefaultState();

            worldIn.setBlockState(pos, solidWall);

            TunnelDefinition tunnelRegistration = tunnelDef.get();
            Item item = tunnelRegistration.getItem();
            ItemStack stack = new ItemStack(item, 1);

            ItemEntity ie = new ItemEntity(worldIn, player.getPosX(), player.getPosY(), player.getPosZ(), stack);
            worldIn.addEntity(ie);

//                        IFormattableTextComponent t = new StringTextComponent(tunnelRegistration.getRegistryName().toString())
//                                .mergeStyle(TextFormatting.GRAY);
//
//                        player.sendStatusMessage(t, true);
        } else {
            // Rotate tunnel
            Direction dir = state.get(TUNNEL_SIDE);
            Direction nextDir = TunnelHelper.getNextDirection(dir);

            worldIn.setBlockState(pos, state.with(TUNNEL_SIDE, nextDir));
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(TUNNEL_SIDE).add(REDSTONE);
        super.fillStateContainer(builder);
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
    public void addProbeInfo(ProbeMode probeMode, IProbeInfo info, PlayerEntity playerEntity, World world, BlockState blockState, IProbeHitData iProbeHitData) {
        Direction side = blockState.get(TUNNEL_SIDE);
        ILayoutStyle center = info.defaultLayoutStyle()
                .alignment(ElementAlignment.ALIGN_CENTER);

        IProbeInfo v = info.vertical(info.defaultLayoutStyle().spacing(-1));

        String sideTranslated = I18n.format(CompactMachines.MODID.concat(".direction.").concat(side.getName2()));
        v
                .horizontal(center)
                .item(new ItemStack(Items.COMPASS))
                .text(new TranslationTextComponent(CompactMachines.MODID + ".direction.side", sideTranslated));

        TunnelWallTile tile = (TunnelWallTile) world.getTileEntity(iProbeHitData.getPos());
        Optional<BlockState> connected = TunnelHelper.getConnectedState(world, tile, EnumTunnelSide.OUTSIDE);
        connected.ifPresent(state -> {
            String blockName = state.getBlock().getTranslatedName().getString();
            v
                    .horizontal(center)
                    .item(new ItemStack(state.getBlock().asItem()))
                    .text(new TranslationTextComponent(CompactMachines.MODID.concat(".connected_block"), blockName));
        });
    }
}
