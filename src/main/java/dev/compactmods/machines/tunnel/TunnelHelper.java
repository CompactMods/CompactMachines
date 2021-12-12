package dev.compactmods.machines.tunnel;

import javax.annotation.Nonnull;
import java.util.Optional;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.core.Tunnels;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegistryObject;

public class TunnelHelper {

    @Nonnull
    public static Direction getNextDirection(Direction in) {
        switch (in) {
            case UP:
                return Direction.DOWN;

            case DOWN:
                return Direction.NORTH;

            case NORTH:
                return Direction.SOUTH;

            case SOUTH:
                return Direction.WEST;

            case WEST:
                return Direction.EAST;

            case EAST:
                return Direction.UP;
        }

        return Direction.UP;
    }

    @Nonnull
    public static Optional<TunnelDefinition> getTunnelDefinitionFromType(ResourceLocation id) {
        Optional<RegistryObject<TunnelDefinition>> first = Tunnels.DEFINITIONS.getEntries()
                .stream()
                .filter(t -> t.get().getRegistryName() == id)
                .findFirst();

        return first.map(RegistryObject::get);

    }
}
