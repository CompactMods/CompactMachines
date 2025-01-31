package dev.compactmods.machines.room.upgrade;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeType;
import dev.compactmods.machines.api.room.upgrade.components.RoomUpgradeList;
import dev.compactmods.machines.feature.CMFeatureFlags;
import dev.compactmods.machines.room.upgrade.example.ChunkLoaderUpgrade;
import dev.compactmods.machines.room.upgrade.example.TreeCutterUpgrade;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.tags.ItemTags;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface RoomUpgrades {

    DeferredRegister<RoomUpgradeType<?>> ROOM_UPGRADE_DEFINITIONS = CompactMachines.roomUpgradeDR(CompactMachines.MOD_ID);

    DeferredHolder<RoomUpgradeType<?>, RoomUpgradeType<TreeCutterUpgrade>> TREECUTTER = ROOM_UPGRADE_DEFINITIONS
            .register("tree_cutter", () -> RoomUpgradeType.builder(TreeCutterUpgrade::new, TreeCutterUpgrade.CODEC)
                    .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
                    .itemPredicate(stack -> stack.is(ItemTags.AXES))
                    .build());

//    DeferredHolder<RoomUpgradeType<?>, RoomUpgradeType<ChunkLoaderUpgrade>> CHUNK_LOADER = ROOM_UPGRADE_DEFINITIONS
//            .register("chunk_loader", () -> RoomUpgradeType.builder(ChunkLoaderUpgrade::new, ChunkLoaderUpgrade.CODEC)
//                    .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
//                    .build());

    static void prepare() {

        TreeCutterUpgrade.prepare();

        ROOM_UPGRADE_DEFINITIONS.makeRegistry(builder -> {
            builder.sync(true);
        });
    }

    static void registerEvents(IEventBus modBus) {
        ROOM_UPGRADE_DEFINITIONS.register(modBus);

        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelLoad);
        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelUnload);
        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelTick);
        NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onTooltips);
    }
}
