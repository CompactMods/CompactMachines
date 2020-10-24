package com.robotgryphon.compactmachines.block.walls;

import com.robotgryphon.compactmachines.block.tiles.TunnelWallTile;
import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.reference.EnumTunnelType;
import com.robotgryphon.compactmachines.tunnels.TunnelDefinition;
import com.robotgryphon.compactmachines.tunnels.TunnelHelper;
import com.robotgryphon.compactmachines.tunnels.TunnelRegistration;
import com.robotgryphon.compactmachines.tunnels.api.IRedstoneTunnel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nullable;
import java.util.Optional;

public class TunnelWallBlock extends WallBlock {
    public static DirectionProperty TUNNEL_SIDE = DirectionProperty.create("tunnel_side", Direction.values());
    public static Property<EnumTunnelType> TUNNEL_TYPE = EnumProperty.create("tunnel_type", EnumTunnelType.class);

    public TunnelWallBlock(Properties props) {
        super(props);
        setDefaultState(getStateContainer().getBaseState()
                .with(TUNNEL_SIDE, Direction.UP)
                .with(TUNNEL_TYPE, EnumTunnelType.ITEM)
        );
    }

    public Optional<TunnelDefinition> getTunnelInfo(BlockState state) {
        if(!(state.getBlock() instanceof TunnelWallBlock))
            return Optional.empty();

        EnumTunnelType enumTunnelType = state.get(TUNNEL_TYPE);
        Optional<RegistryObject<TunnelRegistration>> first = Registrations.TUNNEL_TYPES.getEntries()
                .stream()
                .filter(t -> t.get().getType() == enumTunnelType)
                .findFirst();

        if(!first.isPresent())
            return Optional.empty();

        TunnelRegistration reg = first.get().get();
        return Optional.ofNullable(reg.getDefinition());
    }

    @Override
    public boolean canConnectRedstone(BlockState state, IBlockReader world, BlockPos pos, @Nullable Direction side) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(state);
        if(!tunnelInfo.isPresent())
            return false;

        TunnelDefinition definition = tunnelInfo.get();
        if(definition instanceof IRedstoneTunnel) {
            return ((IRedstoneTunnel) definition).canConnectRedstone(world, state, pos, side);
        }

        return false;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(state);
        if(!tunnelInfo.isPresent())
            return false;

        TunnelDefinition definition = tunnelInfo.get();
        if(definition instanceof IRedstoneTunnel) {
            return ((IRedstoneTunnel) definition).canProvidePower(state);
        }

        return false;
    }

    @Override
    public int getStrongPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(state);
        if(!tunnelInfo.isPresent())
            return 0;

        TunnelDefinition definition = tunnelInfo.get();
        if(definition instanceof IRedstoneTunnel) {
            return ((IRedstoneTunnel) definition).getStrongPower(world, state, pos, side);
        }

        return 0;
    }

    @Override
    public int getWeakPower(BlockState state, IBlockReader world, BlockPos pos, Direction side) {
        Optional<TunnelDefinition> tunnelInfo = getTunnelInfo(state);
        if(!tunnelInfo.isPresent())
            return 0;

        TunnelDefinition definition = tunnelInfo.get();
        if(definition instanceof IRedstoneTunnel) {
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
            Optional<TunnelDefinition> tunnelDef = getTunnelInfo(state);

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
        builder.add(TUNNEL_SIDE).add(TUNNEL_TYPE);
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
}
