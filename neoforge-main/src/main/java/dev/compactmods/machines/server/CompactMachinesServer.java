package dev.compactmods.machines.server;

import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.jetbrains.annotations.Nullable;

@Mod(value = CompactMachines.MOD_ID)
public class CompactMachinesServer {

    private static @Nullable MinecraftServer CURRENT_SERVER;

    public CompactMachinesServer() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverAboutToStart);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverStarting);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::serverStopping);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, CompactMachinesServer::levelSaved);
    }

    private static void serverAboutToStart(final ServerAboutToStartEvent evt) {
        CURRENT_SERVER = evt.getServer();
    }

    private static void serverStarting(final ServerStartingEvent evt) {
        CompactMachines.reloadServices();
    }

    public static void saveAll() {
        if (CURRENT_SERVER != null && CompactMachines.roomApi() != null) {
            CompactMachines.roomApi().save();
            CompactMachines.roomDataAccessor().save();
            CompactMachines.playerHistoryApi().save();
            CompactMachines.upgradeDataAccessor().save();
        }
    }

    public static void serverStopping(final ServerStoppingEvent evt) {
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
