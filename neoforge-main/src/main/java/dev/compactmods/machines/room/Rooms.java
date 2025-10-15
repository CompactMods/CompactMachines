package dev.compactmods.machines.room;

import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.room.block.SolidWallBlock;
import dev.compactmods.machines.room.ui.upgrades.RoomUpgradeMenu;
import dev.compactmods.machines.room.wall.BreakableWallBlock;
import dev.compactmods.machines.room.wall.ItemBlockWall;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

public interface Rooms {

    interface Blocks {
        ResourceKey<Block> SOLID_WALL_KEY = ResourceKey.create(Registries.BLOCK, CompactMachines.modRL("solid_wall"));
        ResourceKey<Block> WALL_KEY = ResourceKey.create(Registries.BLOCK, CompactMachines.modRL("wall"));

        DeferredBlock<SolidWallBlock> SOLID_WALL = CMRegistries.BLOCKS.register("solid_wall", () ->
                new SolidWallBlock(BlockBehaviour.Properties.of()
                        .setId(SOLID_WALL_KEY)
                        .strength(-1.0F, 3600000.8F)
                        .sound(SoundType.METAL)
                        .lightLevel((state) -> 15)));


        DeferredBlock<BreakableWallBlock> BREAKABLE_WALL = CMRegistries.BLOCKS.register("wall", () ->
                new BreakableWallBlock(BlockBehaviour.Properties.of()
                        .setId(WALL_KEY)
                        .strength(3.0f, 128.0f)
                        .requiresCorrectToolForDrops()));

        static void prepare() {
        }
    }

    interface Items {
        Supplier<Item.Properties> WALL_ITEM_PROPS = Item.Properties::new;

        DeferredItem<Item> ROOM_CORE = CMRegistries.ITEMS.register("room_core", () ->
                new Item(new Item.Properties()
                        .setId(ResourceKey.create(Registries.ITEM, CompactMachines.modRL("room_core")))
                        .stacksTo(1)));

        DeferredItem<ItemBlockWall> ITEM_SOLID_WALL = CMRegistries.ITEMS.register("solid_wall", () ->
                new ItemBlockWall(Blocks.SOLID_WALL.get(), WALL_ITEM_PROPS.get()
                        .setId(ResourceKey.create(Registries.ITEM, CompactMachines.modRL("solid_wall")))));

        DeferredItem<ItemBlockWall> BREAKABLE_WALL = CMRegistries.ITEMS.register("wall", () ->
                new ItemBlockWall(Blocks.BREAKABLE_WALL.get(), WALL_ITEM_PROPS.get()
                        .setId(ResourceKey.create(Registries.ITEM, CompactMachines.modRL("wall")))));

        static void prepare() {
        }
    }

    interface Menus {
        DeferredHolder<MenuType<?>, MenuType<RoomUpgradeMenu>> ROOM_UPGRADES = CMRegistries.CONTAINERS.register("room_upgrades",
                () -> IMenuTypeExtension.create(RoomUpgradeMenu::createClientMenu));

        static void prepare() {
        }
    }

    static void prepare() {
        Blocks.prepare();
        Items.prepare();
        Menus.prepare();
    }

    static void registerEvents(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::checkSpawn);
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::entityChangedDimensions);
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::entityJoined);
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::entityTeleport);
    }
}
