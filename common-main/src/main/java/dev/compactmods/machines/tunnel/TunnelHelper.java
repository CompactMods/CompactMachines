package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.core.CMRegistries;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class TunnelHelper {

    public static Registry<TunnelDefinition> definitionRegistry() {
        return RegistryAccess.builtinCopy().registryOrThrow(CMRegistries.TYPES_REG_KEY);
    }

    public static TunnelDefinition getDefinition(ResourceLocation tunnelType) {
        final var reg = definitionRegistry();
        return reg.get(tunnelType);
    }

    @NotNull
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

    public static Stream<Direction> getOrderedSides() {
        return Stream.of(Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    }

    public static Optional<Direction> getNextDirection(Direction current, Set<Direction> used) {
        final var ordered = getOrderedSides().toList();
        final var found = ordered.indexOf(current);
        final var stream = Stream.generate(() -> ordered).flatMap(Collection::stream);

        return stream.skip(found + 1).filter(dir -> !used.contains(dir)).findFirst();
    }


}
