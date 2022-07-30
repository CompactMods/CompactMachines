package dev.compactmods.machines.machine;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Registries;
import dev.compactmods.machines.api.room.RoomSize;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class Machines {
    static final BlockBehaviour.Properties MACHINE_BLOCK_PROPS = BlockBehaviour.Properties
            .of(Material.METAL)
            .strength(8.0F, 20.0F)
            .requiresCorrectToolForDrops();

    static final Supplier<Item.Properties> MACHINE_ITEM_PROPS = () -> new Item.Properties()
            .tab(CompactMachines.COMPACT_MACHINES_ITEMS);

    public static final RegistryObject<Block> MACHINE_BLOCK_TINY = Registries.BLOCKS.register("machine_tiny", () ->
            new CompactMachineBlock(RoomSize.TINY, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_SMALL = Registries.BLOCKS.register("machine_small", () ->
            new CompactMachineBlock(RoomSize.SMALL, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_NORMAL = Registries.BLOCKS.register("machine_normal", () ->
            new CompactMachineBlock(RoomSize.NORMAL, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_LARGE = Registries.BLOCKS.register("machine_large", () ->
            new CompactMachineBlock(RoomSize.LARGE, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_GIANT = Registries.BLOCKS.register("machine_giant", () ->
            new CompactMachineBlock(RoomSize.GIANT, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_MAXIMUM = Registries.BLOCKS.register("machine_maximum", () ->
            new CompactMachineBlock(RoomSize.MAXIMUM, MACHINE_BLOCK_PROPS));




    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_TINY = Registries.ITEMS.register("machine_tiny",
            () -> new CompactMachineItem(MACHINE_BLOCK_TINY.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_SMALL = Registries.ITEMS.register("machine_small",
            () -> new CompactMachineItem(MACHINE_BLOCK_SMALL.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_NORMAL = Registries.ITEMS.register("machine_normal",
            () -> new CompactMachineItem(MACHINE_BLOCK_NORMAL.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_LARGE = Registries.ITEMS.register("machine_large",
            () -> new CompactMachineItem(MACHINE_BLOCK_LARGE.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_GIANT = Registries.ITEMS.register("machine_giant",
            () -> new CompactMachineItem(MACHINE_BLOCK_GIANT.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_MAXIMUM = Registries.ITEMS.register("machine_maximum",
            () -> new CompactMachineItem(MACHINE_BLOCK_MAXIMUM.get(), MACHINE_ITEM_PROPS.get()));



    public static final RegistryObject<BlockEntityType<CompactMachineBlockEntity>> MACHINE_TILE_ENTITY = Registries.BLOCK_ENTITIES.register("compact_machine", () ->
            BlockEntityType.Builder.of(CompactMachineBlockEntity::new,
                            MACHINE_BLOCK_TINY.get(), MACHINE_BLOCK_SMALL.get(), MACHINE_BLOCK_NORMAL.get(),
                            MACHINE_BLOCK_LARGE.get(), MACHINE_BLOCK_GIANT.get(), MACHINE_BLOCK_MAXIMUM.get())
                    .build(null));

    public static void prepare() {

    }
}
