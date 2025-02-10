package dev.compactmods.machines.api.room.upgrade;

import dev.compactmods.machines.api.room.upgrade.event.RoomUpgradeComponentEvent;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.stream.Stream;

public interface RoomUpgradeComponent extends TooltipProvider {

    RoomUpgradeComponentType<?> getType();

    default Stream<RoomUpgradeComponentEvent> gatherEvents() {
        return Stream.empty();
    }
}
