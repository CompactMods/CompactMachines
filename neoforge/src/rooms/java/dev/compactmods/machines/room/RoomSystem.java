package dev.compactmods.machines.room;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;

public final class RoomSystem {

    private RoomSystem() {}

    public static void init(IEventBus modBus) {
        Rooms.prepare();
        registerContent(modBus);

        registerEvents(modBus);
        RoomTemplatesCheckEventHandler.registerEvents();
    }

    static void registerEvents(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::checkSpawn);
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::entityChangedDimensions);
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::entityJoined);
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::entityTeleport);

        NeoForge.EVENT_BUS.addListener(RoomItemHandler::handleTooltips);
    }

    static void registerContent(IEventBus modBus) {
        Rooms.BLOCKS.register(modBus);
        Rooms.ITEMS.register(modBus);
        Rooms.CONTAINERS.register(modBus);
        Rooms.ATTACHMENT_TYPES.register(modBus);
        Rooms.DATA_COMPONENTS.register(modBus);
    }
}
