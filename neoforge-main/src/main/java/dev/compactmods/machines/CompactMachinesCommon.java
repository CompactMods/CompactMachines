package dev.compactmods.machines;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.command.Commands;
import dev.compactmods.machines.compat.InterModCompat;
import dev.compactmods.machines.feature.CMFeaturePacks;
import dev.compactmods.machines.gamerule.CMGameRules;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.dimension.WorldBorderFixer;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.network.CMNetworks;
import dev.compactmods.machines.player.PlayerEventHandler;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.block.ProtectedBlockEventHandler;
import dev.compactmods.machines.room.upgrade.RoomUpgrades;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.villager.Villagers;
import net.minecraft.util.FastColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(CompactMachines.MOD_ID)
public class CompactMachinesCommon {

    public static final int BRAND_MACHINE_COLOR = FastColor.ARGB32.color(255, 248, 246, 76);

    @SuppressWarnings("unused")
    public CompactMachinesCommon(IEventBus modBus, ModContainer modContainer) {
        initConfigs(modContainer);
        prepare();
        registerEvents(modBus);

        CMRegistries.setup(modBus);
    }

    private static void initConfigs(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.CONFIG);
    }

    private static void prepare() {
        Machines.prepare();
        Shrinking.prepare();
        Rooms.prepare();
        RoomUpgrades.prepare();
        Dimension.prepare();
        Commands.prepare();
        Villagers.prepare();
    }

    private static void registerEvents(IEventBus modBus) {
        Rooms.registerEvents(modBus);
        RoomUpgrades.registerEvents(modBus);
        WorldBorderFixer.registerEvents();
        PlayerEventHandler.registerEvents();

        modBus.addListener(CompactMachinesCommon::commonSetup);
        modBus.addListener(CMFeaturePacks::addFeaturePacks);
        modBus.addListener(CMNetworks::onPacketRegistration);
        modBus.addListener(InterModCompat::enqueueCompatMessages);

        NeoForge.EVENT_BUS.addListener(Commands::onCommandsRegister);
        NeoForge.EVENT_BUS.addListener(ProtectedBlockEventHandler::leftClickBlock);
    }

    private static void commonSetup(FMLCommonSetupEvent evt) {
        CMGameRules.register();
    }
}
