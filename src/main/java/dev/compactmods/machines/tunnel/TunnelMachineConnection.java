package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

public class TunnelMachineConnection implements ITunnelConnection {

    private final ServerLevel level;
    private final BlockState state;
    private final Direction side;
    private final BlockPos position;
    private final TunnelDefinition type;

    public TunnelMachineConnection(MinecraftServer server, TunnelWallEntity tunnel) {
        var location = tunnel.getConnectedPosition();
        this.type = tunnel.getTunnelType();
        this.position = location.getBlockPosition();
        this.level = location.level(server).orElseThrow();
        this.state = location.state(server).orElse(Blocks.AIR.defaultBlockState());
        this.side = tunnel.getConnectedSide();
    }

    @Override
    public <T extends TunnelDefinition> T type() {
        return (T) type;
    }

    @Nonnull
    @Override
    public ServerLevel level() {
        return level;
    }

    @Nonnull
    @Override
    public BlockState state() {
        return state;
    }

    /**
     * Gets the side of the machine the tunnel is bound to.
     */
    @Nonnull
    @Override
    public Direction side() {
        return side;
    }

    @Nonnull
    @Override
    public BlockPos position() {
        return position;
    }
}
