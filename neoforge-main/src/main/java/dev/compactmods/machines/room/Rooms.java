package dev.compactmods.machines.room;

import dev.compactmods.machines.CMRegistries;
import dev.compactmods.machines.room.block.SolidWallBlock;
import dev.compactmods.machines.room.ui.upgrades.RoomUpgradeMenu;
import dev.compactmods.machines.room.wall.BreakableWallBlock;
import dev.compactmods.machines.room.wall.ItemBlockWall;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
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
        DeferredBlock<SolidWallBlock> SOLID_WALL = CMRegistries.BLOCKS.register("solid_wall", () ->
                new SolidWallBlock(BlockBehaviour.Properties.of()
                        .strength(-1.0F, 3600000.8F)
                        .sound(SoundType.METAL)
                        .lightLevel((state) -> 15)));


        DeferredBlock<BreakableWallBlock> BREAKABLE_WALL = CMRegistries.BLOCKS.register("wall", () ->
                new BreakableWallBlock(BlockBehaviour.Properties.of()
                        .strength(3.0f, 128.0f)
                        .requiresCorrectToolForDrops()));

        static void prepare() {
        }
    }

    interface Items {
        Supplier<Item.Properties> WALL_ITEM_PROPS = Item.Properties::new;

        DeferredItem<ItemBlockWall> ITEM_SOLID_WALL = CMRegistries.ITEMS.register("solid_wall", () ->
                new ItemBlockWall(Blocks.SOLID_WALL.get(), WALL_ITEM_PROPS.get()));

        DeferredItem<ItemBlockWall> BREAKABLE_WALL = CMRegistries.ITEMS.register("wall", () ->
                new ItemBlockWall(Blocks.BREAKABLE_WALL.get(), WALL_ITEM_PROPS.get()));

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
