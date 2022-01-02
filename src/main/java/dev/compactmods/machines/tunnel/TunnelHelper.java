package dev.compactmods.machines.tunnel;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.stream.Stream;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.api.tunnels.lifecycle.ITunnelSetup;
import dev.compactmods.machines.api.tunnels.lifecycle.ITunnelTeardown;
import dev.compactmods.machines.api.tunnels.lifecycle.TeardownReason;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.core.Tunnels;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.registries.RegistryObject;

public class TunnelHelper {

    @Nonnull
    public static Direction getNextDirection(Direction in) {
        return switch (in) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.NORTH;
            case NORTH -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.EAST;
            case EAST -> Direction.UP;
        };
    }

    @Nonnull
    public static Optional<TunnelDefinition> getTunnelDefinitionFromType(ResourceLocation id) {
        Optional<RegistryObject<TunnelDefinition>> first = Tunnels.DEFINITIONS.getEntries()
                .stream()
                .filter(t -> t.get().getRegistryName() == id)
                .findFirst();

        return first.map(RegistryObject::get);

    }

    public static void teardown(MinecraftServer server, ITunnelConnection connectedPosition, BlockPos tunnelAt, ITunnelTeardown teardown, Direction side, TeardownReason reason) {
        var level = server.getLevel(Registration.COMPACT_DIMENSION);
        var chunk = level.getChunkAt(tunnelAt);
        var room = chunk.getCapability(Capabilities.ROOM);

        if(!room.isPresent()) {
            CompactMachines.LOGGER.fatal("Error tearing down tunnel: required room data not present at {}", tunnelAt);
            return;
        }

        var r = room.resolve().get();

        teardown.teardown(r, new TunnelPosition(level, tunnelAt, side), connectedPosition, reason);
    }

    public static void setup(MinecraftServer server, IDimensionalPosition position, BlockPos tunnelAt, ITunnelSetup setup, Direction side) {
        var level = server.getLevel(Registration.COMPACT_DIMENSION);
        var chunk = level.getChunkAt(tunnelAt);
        var room = chunk.getCapability(Capabilities.ROOM);
        if(!room.isPresent()) {
            CompactMachines.LOGGER.fatal("Error setting up tunnel: required room data not present at {}", tunnelAt);
            return;
        }

        var r = room.resolve().get();

        setup.setup(r, new TunnelPosition(level, tunnelAt, side),  new TunnelMachineConnection(server, (TunnelWallEntity) chunk.getBlockEntity(tunnelAt)));
    }

    public static Stream<Direction> getOrderedSides() {
        return Stream.of(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    }
}
