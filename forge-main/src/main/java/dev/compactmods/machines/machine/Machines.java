package dev.compactmods.machines.machine;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.machine.MachineIds;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.Registries;
import dev.compactmods.machines.machine.block.CompactMachineBlock;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.block.LegacySizedCompactMachineBlock;
import dev.compactmods.machines.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.machine.item.LegacyCompactMachineItem;
import dev.compactmods.machines.machine.item.UnboundCompactMachineItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@SuppressWarnings("removal")
public class Machines {
    static final BlockBehaviour.Properties MACHINE_BLOCK_PROPS = BlockBehaviour.Properties
            .of(Material.METAL)
            .strength(8.0F, 20.0F)
            .requiresCorrectToolForDrops();

    static final Supplier<Item.Properties> MACHINE_ITEM_PROPS = Item.Properties::new;

    public static final RegistryObject<Block> MACHINE_BLOCK_TINY = Registries.BLOCKS.register("machine_tiny", () ->
            new LegacySizedCompactMachineBlock(RoomSize.TINY, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_SMALL = Registries.BLOCKS.register("machine_small", () ->
            new LegacySizedCompactMachineBlock(RoomSize.SMALL, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_NORMAL = Registries.BLOCKS.register("machine_normal", () ->
            new LegacySizedCompactMachineBlock(RoomSize.NORMAL, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_LARGE = Registries.BLOCKS.register("machine_large", () ->
            new LegacySizedCompactMachineBlock(RoomSize.LARGE, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_GIANT = Registries.BLOCKS.register("machine_giant", () ->
            new LegacySizedCompactMachineBlock(RoomSize.GIANT, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK_MAXIMUM = Registries.BLOCKS.register("machine_maximum", () ->
            new LegacySizedCompactMachineBlock(RoomSize.MAXIMUM, MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Block> MACHINE_BLOCK = Registries.BLOCKS.register("machine", () ->
            new CompactMachineBlock(MACHINE_BLOCK_PROPS));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_TINY = Registries.ITEMS.register("machine_tiny",
            () -> new LegacyCompactMachineItem(MACHINE_BLOCK_TINY.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_SMALL = Registries.ITEMS.register("machine_small",
            () -> new LegacyCompactMachineItem(MACHINE_BLOCK_SMALL.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_NORMAL = Registries.ITEMS.register("machine_normal",
            () -> new LegacyCompactMachineItem(MACHINE_BLOCK_NORMAL.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_LARGE = Registries.ITEMS.register("machine_large",
            () -> new LegacyCompactMachineItem(MACHINE_BLOCK_LARGE.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_GIANT = Registries.ITEMS.register("machine_giant",
            () -> new LegacyCompactMachineItem(MACHINE_BLOCK_GIANT.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> MACHINE_BLOCK_ITEM_MAXIMUM = Registries.ITEMS.register("machine_maximum",
            () -> new LegacyCompactMachineItem(MACHINE_BLOCK_MAXIMUM.get(), MACHINE_ITEM_PROPS.get()));

    public static final RegistryObject<Item> BOUND_MACHINE_BLOCK_ITEM = Registries.ITEMS.register("machine",
            () -> new BoundCompactMachineItem(MACHINE_BLOCK.get(), MACHINE_ITEM_PROPS.get().tab(CompactMachines.COMPACT_MACHINES_ITEMS)));


    public static final RegistryObject<Item> UNBOUND_MACHINE_BLOCK_ITEM = Registries.ITEMS.register("new_machine",
            () -> new UnboundCompactMachineItem(MACHINE_BLOCK.get(), MACHINE_ITEM_PROPS.get().tab(CompactMachines.COMPACT_MACHINES_ITEMS)));

    public static final RegistryObject<BlockEntityType<CompactMachineBlockEntity>> MACHINE_TILE_ENTITY = Registries.BLOCK_ENTITIES.register(MachineIds.BLOCK_ENTITY.getPath(), () ->
            BlockEntityType.Builder.of(CompactMachineBlockEntity::new,
                            MACHINE_BLOCK.get(),
                            MACHINE_BLOCK_TINY.get(), MACHINE_BLOCK_SMALL.get(), MACHINE_BLOCK_NORMAL.get(),
                            MACHINE_BLOCK_LARGE.get(), MACHINE_BLOCK_GIANT.get(), MACHINE_BLOCK_MAXIMUM.get())
                    .build(null));

    public static void prepare() {

    }
}
