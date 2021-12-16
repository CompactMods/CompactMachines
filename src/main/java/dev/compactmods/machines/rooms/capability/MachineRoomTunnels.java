package dev.compactmods.machines.rooms.capability;

import java.util.HashMap;
import java.util.stream.Stream;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.IMachineTunnels;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.block.tiles.TunnelWallEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class MachineRoomTunnels implements IMachineTunnels {
    private final LevelChunk chunk;
    private final HashMap<BlockPos, TunnelDefinition> tunnels;

    public MachineRoomTunnels(LevelChunk chunk) {
        this.chunk = chunk;
        this.tunnels = new HashMap<>();
    }

    /**
     * Creates a stream of registered tunnels, given a connected machine position.
     * May contain multiple tunnels, as tunnels can handle individual tasks
     * that are combined in one block (ie Thermal Signalum-plated, EnderIO conduits)
     *
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    @Override
    public Stream<ITunnelConnection> getTunnels() {
        return tunnels.keySet().stream()
                .filter(pos -> chunk.getBlockState(pos).hasBlockEntity())
                .map(chunk::getBlockEntity)
                .filter(ent -> ent instanceof TunnelWallEntity)
                .map(tunnel -> {
                    return ((TunnelWallEntity) tunnel).getConnection();
                });
    }
}
