package dev.compactmods.machines;

import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.stream.Stream;

public interface CMRegistries {

	// Machines, Walls, Shrinking
	DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CompactMachines.MOD_ID);
	DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactMachines.MOD_ID);

	DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, CompactMachines.MOD_ID);

	DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, CompactMachines.MOD_ID);

	// UIRegistration
	DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, CompactMachines.MOD_ID);

	// Commands
	DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, CompactMachines.MOD_ID);

	// LootFunctions
	DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS = DeferredRegister.create(BuiltInRegistries.LOOT_FUNCTION_TYPE, CompactMachines.MOD_ID);

	// Villagers
	DeferredRegister<VillagerProfession> VILLAGERS = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, CompactMachines.MOD_ID);

	DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, CompactMachines.MOD_ID);

	static Item basicItem() {
		return new Item(new Item.Properties());
	}

	static void setup(IEventBus modBus) {
		Stream.of(BLOCKS, ITEMS, BLOCK_ENTITIES, CONTAINERS, COMMAND_ARGUMENT_TYPES, LOOT_FUNCTIONS,
			VILLAGERS, POINTS_OF_INTEREST, TABS,
				CMDataAttachments.ATTACHMENT_TYPES,
				CMDataComponents.DATA_COMPONENTS
		).forEach(r -> r.register(modBus));

		modBus.addListener((DataPackRegistryEvent.NewRegistry newRegistries) -> {
			newRegistries.dataPackRegistry(RoomTemplate.REGISTRY_KEY, RoomTemplate.CODEC, RoomTemplate.CODEC);
		});

		modBus.addListener((BuildCreativeModeTabContentsEvent addToTabs) -> {
			if (addToTabs.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
				addToTabs.accept(Shrinking.PERSONAL_SHRINKING_DEVICE.get());
			}
		});
	}
}
