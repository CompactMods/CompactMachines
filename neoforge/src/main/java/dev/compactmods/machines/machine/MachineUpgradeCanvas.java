package dev.compactmods.machines.machine;

import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.room.CMFeatureFlags;
import dev.compactmods.machines.upgrades.canvas.RoomCanvases;
import dev.robotgryphon.screenlib.nodes.api.ScreenLibCapabilities;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Exposes a bound machine's room upgrade canvas as screenlib's {@link ScreenLibCapabilities#CANVAS}
 * block capability, which the machine menu hosts for the room upgrades panel. Lives in the main mod
 * because it ties machines to room upgrades. Absent unless the room upgrades feature is enabled.
 */
public final class MachineUpgradeCanvas {

    private MachineUpgradeCanvas() {}

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(ScreenLibCapabilities.CANVAS, Machines.BlockEntities.MACHINE.get(), (machine, _) -> {
            if (!(machine.getLevel() instanceof ServerLevel level) || !CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(level.enabledFeatures()))
                return null;

            final var rooms = RoomCapabilities.REGISTRY.getCapability(level.getServer());
            if (rooms == null)
                return null;

            return machine.connectedRoom()
                    .flatMap(rooms::get)
                    .map(room -> room.getCapability(RoomCanvases.CANVAS))
                    .orElse(null);
        });
    }
}
