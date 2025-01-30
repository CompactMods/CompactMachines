package dev.compactmods.machines.room.upgrade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeType;
import dev.compactmods.machines.api.room.upgrade.components.RoomUpgradeList;
import dev.compactmods.machines.feature.CMFeatureFlags;
import dev.compactmods.machines.room.upgrade.example.LightsOutUpgrade;
import dev.compactmods.machines.room.upgrade.example.TreeCutterUpgrade;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface RoomUpgrades {

  DeferredRegister<RoomUpgradeType<?>> ROOM_UPGRADE_DEFINITIONS = CompactMachines.roomUpgradeDR(CompactMachines.MOD_ID);

  DeferredHolder<DataComponentType<?>, DataComponentType<RoomUpgradeList>> UPGRADE_LIST_COMPONENT = CMRegistries.DATA_COMPONENTS
      .registerComponentType("room_upgrades", (builder) -> builder
          .persistent(RoomUpgradeList.CODEC)
          .networkSynchronized(RoomUpgradeList.STREAM_CODEC));

  DeferredHolder<RoomUpgradeType<?>, RoomUpgradeType<TreeCutterUpgrade>> TREECUTTER = ROOM_UPGRADE_DEFINITIONS
      .register("tree_cutter", () -> RoomUpgradeType.builder(TreeCutterUpgrade::new, TreeCutterUpgrade.CODEC)
              .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
              .itemPredicate(stack -> stack.is(ItemTags.AXES))
              .build());

  DeferredHolder<RoomUpgradeType<?>, RoomUpgradeType<LightsOutUpgrade>> LIGHTS_OUT_UPGRADE = ROOM_UPGRADE_DEFINITIONS
          .register("lights_out", () -> RoomUpgradeType.builder(LightsOutUpgrade::new, LightsOutUpgrade.CODEC)
                  .requiredFeatures(CMFeatureFlags.ROOM_UPGRADES)
                  .itemPredicate(stack -> stack.is(Items.REDSTONE_LAMP) || stack.is(Items.COPPER_BULB))
                  .build());

  static void prepare() {
    ROOM_UPGRADE_DEFINITIONS.makeRegistry(builder -> {
      builder.sync(true);
    });
  }

  static void registerEvents(IEventBus modBus) {
    ROOM_UPGRADE_DEFINITIONS.register(modBus);

    NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onLevelTick);
    NeoForge.EVENT_BUS.addListener(RoomUpgradeEventHandlers::onTooltips);
  }
}
