package dev.compactmods.machines.rooms.capability;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.IRoomTunnels;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.block.tiles.TunnelWallEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class MachineRoomTunnels implements IRoomTunnels {
    private final LevelChunk chunk;
    private final HashMap<BlockPos, TunnelDefinition> tunnels;

    public MachineRoomTunnels(LevelChunk chunk) {
        this.chunk = chunk;
        this.tunnels = new HashMap<>();
    }

    /**
     * Registers a new tunnel applied to a position inside a machine room.
     *
     * @param type   The type of tunnel being registered.
     * @param at The position of the tunnel being registered.
     * @return True if successfully registered, false otherwise.
     */
    @Override
    public <T extends TunnelDefinition> boolean register(T type, BlockPos at) {
        if(tunnels.containsKey(at))
            return false;

        tunnels.put(at, type);
        return true;
    }

    @Override
    public boolean unregister(BlockPos at) {
        if(!tunnels.containsKey(at))
            return false;

        tunnels.remove(at);
        return true;
    }

    /**
     * Creates a stream of registered tunnels, given a connected machine position.
     * May contain multiple tunnels, as tunnels can handle individual tasks
     * that are combined in one block (ie Thermal Signalum-plated, EnderIO conduits)
     *
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    @Override
    public Stream<ITunnelConnection> stream() {
        return tunnels.keySet().stream()
                .filter(pos -> chunk.getBlockState(pos).hasBlockEntity())
                .map(chunk::getBlockEntity)
                .filter(ent -> ent instanceof TunnelWallEntity)
                .map(tunnel -> {
                    return ((TunnelWallEntity) tunnel).getConnection();
                });
    }

    @Override
    public Optional<ITunnelConnection> locatedAt(BlockPos pos) {
        if(!tunnels.containsKey(pos))
            return Optional.empty();

        if(chunk.getBlockState(pos).hasBlockEntity()) {
            if(chunk.getBlockEntity(pos) instanceof TunnelWallEntity t) {
                return Optional.of(t.getConnection());
            }
        }

        return Optional.empty();
    }

    @Override
    public Stream<BlockPos> stream(TunnelDefinition type) {
        return tunnels.keySet()
                .stream()
                .filter(pos -> tunnels.get(pos).getRegistryName() != null)
                .filter(pos -> {
                    var regName = tunnels.get(pos).getRegistryName();
                    return regName != null && regName.equals(type.getRegistryName());
                });
    }
}
