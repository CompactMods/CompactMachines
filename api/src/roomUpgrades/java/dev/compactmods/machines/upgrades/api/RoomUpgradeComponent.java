package dev.compactmods.machines.upgrades.api;

import dev.compactmods.machines.upgrades.api.event.RoomUpgradeComponentEvent;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.stream.Stream;

public interface RoomUpgradeComponent extends TooltipProvider {

    RoomUpgradeComponentType<?> getType();

    default Stream<RoomUpgradeComponentEvent> gatherEvents() {
        return Stream.empty();
    }
}
