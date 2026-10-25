package dev.compactmods.machines.upgrades;

import dev.compactmods.machines.upgrades.api.RoomUpgradeComponentType;
import dev.compactmods.machines.upgrades.api.RoomUpgradesApi;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.CMFeatureFlags;
import dev.compactmods.machines.upgrades.canvas.RoomCanvases;
import dev.compactmods.machines.upgrades.debug.DebugStorages;
import dev.compactmods.machines.upgrades.command.RoomUpgradesSubcommand;
import dev.compactmods.machines.upgrades.example.ChunkLoaderTickets;
import dev.compactmods.machines.upgrades.example.ChunkLoaderUpgradeComponent;
import dev.compactmods.machines.upgrades.example.TreeCutterUpgradeComponent;
import dev.compactmods.machines.upgrades.example.TreeCutterState;
import dev.compactmods.machines.upgrades.storage.ResourceTypes;
import dev.compactmods.machines.upgrades.storage.StorageCapabilities;
import dev.compactmods.machines.upgrades.system.RoomSystems;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RoomUpgrades {

    interface RURegistries {
        DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CompactMachinesCore.MOD_ID);

        DeferredRegister<RoomUpgradeComponentType<?>> ROOM_UPGRADE_DEFINITIONS = RoomUpgradesApi.roomUpgradeDR(CompactMachinesCore.MOD_ID);
    }

    // The old item-stored RoomUpgradeComponentList (UPGRADE_LIST_COMPONENT data component) is scrapped:
    // a room's upgrades now live in CompiledRoomUpgrade bundles enabled per room (see RoomSystems.ENABLED_UPGRADES).

    public static final DeferredHolder<RoomUpgradeComponentType<?>, RoomUpgradeComponentType<TreeCutterUpgradeComponent>> TREECUTTER = RURegistries.ROOM_UPGRADE_DEFINITIONS
            .register("tree_cutter", () -> RoomUpgradeComponentType.builder(TreeCutterUpgradeComponent::new)
                    .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
                    .build());

    public static final DeferredHolder<RoomUpgradeComponentType<?>, RoomUpgradeComponentType<ChunkLoaderUpgradeComponent>> CHUNK_LOADER = RURegistries.ROOM_UPGRADE_DEFINITIONS
            .register("chunk_loader", () -> RoomUpgradeComponentType.builder(ChunkLoaderUpgradeComponent::new)
                    .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
                    .build());

    public static void init(IEventBus modBus) {

        RURegistries.ROOM_UPGRADE_DEFINITIONS.makeRegistry(builder -> {
            builder.sync(true);
        });

        registerEvents(modBus);

        modBus.addListener(RoomUpgrades::onRegisterCapabilities);
        modBus.addListener(ChunkLoaderTickets::register);

        RURegistries.DATA_COMPONENTS.register(modBus);
        RURegistries.ROOM_UPGRADE_DEFINITIONS.register(modBus);

        ResourceTypes.init(modBus);

        RoomSystems.init(modBus);
        RoomCanvases.init();
        DebugStorages.register(modBus);
        TreeCutterState.init();
    }

    static void registerEvents(IEventBus modBus) {
        modBus.addListener(RoomUpgrades::commonSetup);

        NeoForge.EVENT_BUS.addListener(RoomUpgrades::onCommandsRegister);
        
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelLoad);
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelUnload);
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelTick);
//        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onTooltips);
    }

    private static void commonSetup(FMLCommonSetupEvent evt) {
//        evt.enqueueWork(RoomUpgradeEventHandlers::collectUpgradeEvents);
    }

    private static void onCommandsRegister(final RegisterCommandsEvent event) {
        CompactMachinesCore.CM_COMMAND_ROOT.then(RoomUpgradesSubcommand.make());
    }

    static void onRegisterCapabilities(RegisterCapabilitiesEvent r) {
        StorageCapabilities.registerProviders();
        RoomCanvases.registerProviders();
    }
}
