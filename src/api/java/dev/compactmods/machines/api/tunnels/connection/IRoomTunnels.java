package dev.compactmods.machines.api.tunnels.connection;

import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.core.BlockPos;

/**
 * Provides information about tunnels inside a machine room.
 * Accessed by querying the chunk the machine room is inside.
 */
public interface IRoomTunnels {

    <T extends TunnelDefinition> boolean register(T type, BlockPos at);

    boolean unregister(BlockPos at);

    /**
     * Creates a stream of tunnel connections.
     *
     * @return Stream of tunnel connection information, for each connected tunnel
     */
    Stream<ITunnelConnection> stream();

    /**
     * Creates a stream of tunnel locations, untyped.
     *
     * @return Stream of positions inside a room that have tunnels at them.
     */
    Stream<BlockPos> streamLocations();

    /**
     * Gets information about a given tunnel position.
     * @param pos
     * @return
     */
    Optional<ITunnelConnection> locatedAt(BlockPos pos);

    /**
     * Gets a stream of tunnel positions whose type definition matches the passed value.
     *
     * @param type
     * @return
     */
    Stream<BlockPos> stream(TunnelDefinition type);
}
