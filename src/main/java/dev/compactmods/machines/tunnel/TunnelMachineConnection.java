package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.block.tiles.TunnelWallEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class TunnelMachineConnection implements ITunnelConnection {

    private final TunnelDefinition tunnelType;
    private IDimensionalPosition location;
    private BlockState state;
    private final Direction side;
    private final LazyOptional<IDimensionalPosition> lazy;

    public TunnelMachineConnection(Level level, TunnelWallEntity tunnel) {
        this.tunnelType = tunnel.getTunnelType();
        this.location = tunnel.getConnectedPosition();
        this.lazy = LazyOptional.of(this::position);
        this.state = location.getBlockState(level.getServer()).orElse(Blocks.AIR.defaultBlockState());
        this.side = tunnel.getConnectedSide();
    }

    @NotNull
    @Override
    public TunnelDefinition tunnelType() {
        return tunnelType;
    }

    @NotNull
    @Override
    public IDimensionalPosition position() {
        return location;
    }

    @NotNull
    @Override
    public BlockState state() {
        return state;
    }

    /**
     * Gets the side of the machine the tunnel is bound to.
     */
    @NotNull
    @Override
    public Direction side() {
        return side;
    }

    public void invalidatePosition() {
        lazy.invalidate();
    }

    public void setLocation(IDimensionalPosition pos) {
        this.location = pos;
        this.state = location.getBlockState(ServerLifecycleHooks.getCurrentServer()).orElse(Blocks.AIR.defaultBlockState());
        this.lazy.invalidate();
    }
}
