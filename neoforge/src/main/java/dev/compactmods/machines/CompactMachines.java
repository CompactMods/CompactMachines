package dev.compactmods.machines;

import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.feature.CMFeaturePacks;
import dev.compactmods.machines.gamerule.CMGameRules;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.MachineUpgradeCanvas;
import dev.compactmods.machines.network.CMNetworks;
import dev.compactmods.machines.room.preview.server.RoomPreviewService;
import dev.compactmods.machines.room.RoomSystem;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.upgrades.RoomUpgrades;
import dev.compactmods.machines.room.block.ProtectedBlockEventHandler;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.util.ARGB;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.CrashReportCallables;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

@Mod(CompactMachinesCore.MOD_ID)
public class CompactMachines {

    public static final int BRAND_MACHINE_COLOR = ARGB.color(255, 248, 246, 76);

    @SuppressWarnings("unused")
    public CompactMachines(IEventBus modBus) {
        Commands.prepare();
        
        CMDataAttachments.prepare();

        registerEvents(modBus);

        CMRegistries.setup(modBus);

        Dimension.init(modBus);
        CMGameRules.init(modBus);
        Villagers.init(modBus);
        Machines.init(modBus);
        RoomSystem.init(modBus);
        RoomUpgrades.init(modBus);
        modBus.addListener(MachineUpgradeCanvas::registerCapabilities);
        Shrinking.init(modBus);

        CrashReportCallables.registerCrashCallable("Compact Machines", CompactMachines::crashReport);
    }

    private static String crashReport() {
        final var server = ServerLifecycleHooks.getCurrentServer();
        if(server == null)
            return "<No Server, no game rule info for you>";

        final var gameRules = server.getGameRules();
        final var line = System.lineSeparator();

        //noinspection StringBufferReplaceableByString
        return new StringBuilder()
            .append(line)
            .append("\t\tAllow Survival OOB: ")
                .append(gameRules.get(CMGameRules.ALLOW_SURVIVAL_OUT_OF_BOUNDS.value()))
                .append(line)
            .append("\t\tAllow Creative OOB: ")
                .append(gameRules.get(CMGameRules.ALLOW_CREATIVE_OUT_OF_BOUNDS.value()))
                .append(line)
            .append("\t\tAllow Big Rooms: ")
                .append(gameRules.get(CMGameRules.ALLOW_BIG_ROOMS.value()))
                .append(line)
            .toString();
    }

    private static void registerEvents(IEventBus modBus) {
        modBus.addListener(CMFeaturePacks::addFeaturePacks);
        modBus.addListener(CMNetworks::onPacketRegistration);

        NeoForge.EVENT_BUS.addListener(Commands::onCommandsRegister);
        NeoForge.EVENT_BUS.addListener(ProtectedBlockEventHandler::leftClickBlock);

        // Room preview pipeline: capture loaded-room interiors and push snapshots to subscribed clients.
        NeoForge.EVENT_BUS.addListener(RoomPreviewService::onLevelTick);
        NeoForge.EVENT_BUS.addListener(RoomPreviewService::onPlayerLoggedOut);
        NeoForge.EVENT_BUS.addListener(RoomPreviewService::onServerStopping);
    }
}
