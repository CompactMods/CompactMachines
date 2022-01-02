package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class TunnelMachineConnection implements ITunnelConnection {

    private final ServerLevel level;
    private final BlockState state;
    private final Direction side;
    private final BlockPos position;

    public TunnelMachineConnection(MinecraftServer server, TunnelWallEntity tunnel) {
        var location = tunnel.getConnectedPosition();
        this.position = location.getBlockPosition();
        this.level = location.level(server).orElseThrow();
        this.state = location.state(server).orElse(Blocks.AIR.defaultBlockState());
        this.side = tunnel.getConnectedSide();
    }

    @NotNull
    @Override
    public ServerLevel level() {
        return level;
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

    @NotNull
    @Override
    public BlockPos position() {
        return position;
    }
}
