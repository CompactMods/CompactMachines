package dev.compactmods.machines.room.upgrade;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponentType;
import dev.compactmods.machines.feature.CMFeatureFlags;
import dev.compactmods.machines.room.upgrade.example.ChunkLoaderUpgradeComponent;
import dev.compactmods.machines.room.upgrade.example.TreeCutterUpgradeComponent;
import dev.compactmods.machines.room.upgrade.event.NeoForgeEventProcessor;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.tags.ItemTags;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Map;

public interface RoomUpgrades {

    Map<Class<? extends Event>, NeoForgeEventProcessor<?>> EVENT_PROCESSORS = new Reference2ObjectArrayMap<>();

    DeferredRegister<RoomUpgradeComponentType<?>> ROOM_UPGRADE_DEFINITIONS = CompactMachines.roomUpgradeDR(CompactMachines.MOD_ID);

    DeferredHolder<RoomUpgradeComponentType<?>, RoomUpgradeComponentType<TreeCutterUpgradeComponent>> TREECUTTER = ROOM_UPGRADE_DEFINITIONS
            .register("tree_cutter", () -> RoomUpgradeComponentType.builder(TreeCutterUpgradeComponent::new, TreeCutterUpgradeComponent.CODEC)
                    .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
                    .itemPredicate(stack -> stack.is(ItemTags.AXES))
                    .build());

    DeferredHolder<RoomUpgradeComponentType<?>, RoomUpgradeComponentType<ChunkLoaderUpgradeComponent>> CHUNK_LOADER = ROOM_UPGRADE_DEFINITIONS
            .register("chunk_loader", () -> RoomUpgradeComponentType.builder(ChunkLoaderUpgradeComponent::new, ChunkLoaderUpgradeComponent.CODEC)
                    .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
                    .build());

    static void prepare() {

        TreeCutterUpgradeComponent.prepare();

        ROOM_UPGRADE_DEFINITIONS.makeRegistry(builder -> {
            builder.sync(true);
        });
    }

    static void registerEvents(IEventBus modBus) {
        ROOM_UPGRADE_DEFINITIONS.register(modBus);

        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::cleanupDeadUpgrades);
        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelLoad);
        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelTick);
        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onTooltips);
    }

    @SuppressWarnings("unchecked")
    static <TEvt extends Event> NeoForgeEventProcessor<TEvt> eventProcessor(final Class<TEvt> event) {
        return (NeoForgeEventProcessor<TEvt>) EVENT_PROCESSORS.computeIfAbsent(event, eClass -> {
            var processor = new NeoForgeEventProcessor<>(event);
            NeoForge.EVENT_BUS.addListener(processor.type(), processor::process);
            return processor;
        });
    }
}
