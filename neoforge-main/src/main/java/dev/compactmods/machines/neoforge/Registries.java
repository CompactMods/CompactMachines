package dev.compactmods.machines.neoforge;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
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
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static dev.compactmods.machines.api.Constants.MOD_ID;

public interface Registries {

    // Machines, Walls, Shrinking
    DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MOD_ID);

    DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MOD_ID);

    // UIRegistration
    DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, MOD_ID);

    // Commands
    DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(BuiltInRegistries.COMMAND_ARGUMENT_TYPE, MOD_ID);

    // LootFunctions
    DeferredRegister<LootItemFunctionType> LOOT_FUNCS = DeferredRegister.create(BuiltInRegistries.LOOT_FUNCTION_TYPE, MOD_ID);

    // Villagers
    DeferredRegister<VillagerProfession> VILLAGERS = DeferredRegister.create(BuiltInRegistries.VILLAGER_PROFESSION, Constants.MOD_ID);

    DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, Constants.MOD_ID);

    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);

    static Item basicItem() {
        return new Item(new Item.Properties());
    }

    static void setup(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        CONTAINERS.register(modBus);
        COMMAND_ARGUMENT_TYPES.register(modBus);
        LOOT_FUNCS.register(modBus);
        VILLAGERS.register(modBus);
        // Villagers.TRADES.register(bus);
        POINTS_OF_INTEREST.register(modBus);
        TABS.register(modBus);
        ATTACHMENT_TYPES.register(modBus);

        modBus.addListener((DataPackRegistryEvent.NewRegistry newRegistries) -> {
            newRegistries.dataPackRegistry(RoomTemplate.REGISTRY_KEY, RoomTemplate.CODEC, RoomTemplate.CODEC);
        });

        modBus.addListener((BuildCreativeModeTabContentsEvent addToTabs) -> {
            if(addToTabs.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
                addToTabs.accept(Shrinking.PERSONAL_SHRINKING_DEVICE.get());
            }
        });
    }
}
