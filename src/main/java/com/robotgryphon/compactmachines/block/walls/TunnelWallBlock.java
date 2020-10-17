package com.robotgryphon.compactmachines.block.walls;

import com.robotgryphon.compactmachines.reference.EnumTunnelType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;

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

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(TUNNEL_SIDE).add(TUNNEL_TYPE);
        super.fillStateContainer(builder);
    }
}
