package dev.compactmods.machines.neoforge.room;

import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.neoforge.Registries;
import dev.compactmods.machines.neoforge.room.block.SolidWallBlock;
import dev.compactmods.machines.neoforge.room.ui.MachineRoomMenu;
import dev.compactmods.machines.wall.BreakableWallBlock;
import dev.compactmods.machines.wall.ItemBlockWall;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

public interface Rooms {

    Supplier<Item.Properties> WALL_ITEM_PROPS = Item.Properties::new;

    DeferredBlock<SolidWallBlock> BLOCK_SOLID_WALL = Registries.BLOCKS.register("solid_wall", () ->
            new SolidWallBlock(BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.8F)
                    .sound(SoundType.METAL)
                    .lightLevel((state) -> 15)));
    DeferredItem<ItemBlockWall> ITEM_SOLID_WALL = Registries.ITEMS.register("solid_wall", () ->
            new ItemBlockWall(BLOCK_SOLID_WALL.get(), WALL_ITEM_PROPS.get()));
    DeferredBlock<BreakableWallBlock> BLOCK_BREAKABLE_WALL = Registries.BLOCKS.register("wall", () ->
            new BreakableWallBlock(BlockBehaviour.Properties.of()
                    .strength(3.0f, 128.0f)
                    .requiresCorrectToolForDrops()));
    DeferredItem<ItemBlockWall> ITEM_BREAKABLE_WALL = Registries.ITEMS.register("wall", () ->
            new ItemBlockWall(BLOCK_BREAKABLE_WALL.get(), WALL_ITEM_PROPS.get()));
    DeferredHolder<MenuType<?>, MenuType<MachineRoomMenu>> MACHINE_MENU = Registries.CONTAINERS.register("machine",
            () -> IMenuTypeExtension.create(MachineRoomMenu::createRoomMenu));

    Supplier<AttachmentType<RoomEntryPoint>> LAST_ROOM_ENTRYPOINT = Registries.ATTACHMENT_TYPES.register("last_entrypoint", () -> AttachmentType.builder(() -> RoomEntryPoint.INVALID)
            .serialize(RoomEntryPoint.CODEC)
            .build());

    static void prepare() {
    }
}
