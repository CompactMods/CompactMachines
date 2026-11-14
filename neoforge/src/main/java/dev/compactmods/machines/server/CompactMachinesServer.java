package dev.compactmods.machines.server;

import dev.compactmods.machines.CMDataAttachments;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.data.Saveable;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.room.data.CMRoomDataMigration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import java.util.function.Supplier;

@Mod(value = CompactMachinesCore.MOD_ID)
public class CompactMachinesServer {

    public static final Supplier<AttachmentType<IServerCapabilities>> SERVER_CAPABILITIES = CMDataAttachments.ATTACHMENT_TYPES
            .register("server_capabilities", () -> AttachmentType
                .<IServerCapabilities>builder((server) -> new ServerCapabilities((MinecraftServer) server))
                .build());

    public CompactMachinesServer(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, CompactMachinesServer::serverAboutToStart);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, CompactMachinesServer::serverStarted);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverStopping);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::levelSaved);

        modBus.addListener(CompactMachinesServer::registerTicketController);
        modBus.addListener(ServerCapabilities::register);
    }

    private static void registerTicketController(RegisterTicketControllersEvent event) {
//        event.register(CHUNK_TICKET_CONTROLLER);
    }

    private static void saveAll(MinecraftServer server) {
        final var caps = server.getData(SERVER_CAPABILITIES);
        if(caps instanceof Saveable s)
            s.save();
    }

    private static void serverAboutToStart(final ServerAboutToStartEvent event) {
        // Before anything reads room data from disk
        CMRoomDataMigration.migrateLegacyLayout(event.getServer());
    }

    private static void serverStarted(final ServerStartedEvent started) {
        final var server = started.getServer();
        final var caps = server.getData(SERVER_CAPABILITIES);
        caps.chunkManager().initializeCache();
    }

    private static void serverStopping(final ServerStoppingEvent stopping) {
        final var server = stopping.getServer();
        saveAll(server);
    }

    private static void levelSaved(final LevelEvent.Save level) {
        if (level.getLevel() instanceof Level l && CompactDimension.isLevelCompact(l)) {
            saveAll(l.getServer());
        }
    }
}
