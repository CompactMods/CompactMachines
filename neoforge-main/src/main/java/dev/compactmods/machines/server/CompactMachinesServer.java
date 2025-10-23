package dev.compactmods.machines.server;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.data.Saveable;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.room.upgrade.RoomUpgradeHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

@Mod(value = CompactMachines.MOD_ID)
public class CompactMachinesServer {

    private static @Nullable MinecraftServer CURRENT_SERVER;

    public static TicketController CHUNK_TICKET_CONTROLLER = new TicketController(CompactMachines.modRL("chunkloader_upgrade"),
            (serverLevel, ticketHelper) -> RoomUpgradeHelper.verifyChunkloaderUpgrades(ticketHelper));

    public CompactMachinesServer(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverAboutToStart);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverStarting);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverStopping);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::levelSaved);

        modBus.addListener(CompactMachinesServer::registerTicketController);
    }

    private static void registerTicketController(RegisterTicketControllersEvent event) {
        event.register(CHUNK_TICKET_CONTROLLER);
    }

    private static void serverAboutToStart(final ServerAboutToStartEvent evt) {
        CURRENT_SERVER = evt.getServer();
        CompactMachines.reloadServices(CURRENT_SERVER);
    }

    private static void serverStarting(final ServerStartingEvent evt) {
        CompactMachines.reloadServices(evt.getServer());
    }

    public static void saveAll() {
        if (CURRENT_SERVER != null) {
            Stream.of(CompactMachines.roomRegistrar(), CompactMachines.roomDataAccessor(),
                            CompactMachines.playerHistoryApi(),
                            CompactMachines.spawnManagers(),
                            CompactMachines.upgradeDataAccessor())
                    .filter(Objects::nonNull)
                    .forEach(Saveable::save);
        }
    }

    public static void serverStopping(final ServerStoppingEvent ignored) {
        saveAll();
    }

    public static void levelSaved(final LevelEvent.Save level) {
        if (level.getLevel() instanceof Level l && CompactDimension.isLevelCompact(l)) {
            saveAll();
        }
    }

    public static MinecraftServer currentServer() {
        return CURRENT_SERVER;
    }
}
