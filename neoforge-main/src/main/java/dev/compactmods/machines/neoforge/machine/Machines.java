package dev.compactmods.machines.neoforge.machine;

import dev.compactmods.machines.api.machine.MachineConstants;
import dev.compactmods.machines.neoforge.Registries;
import dev.compactmods.machines.neoforge.machine.block.BoundCompactMachineBlock;
import dev.compactmods.machines.neoforge.machine.block.UnboundCompactMachineBlock;
import dev.compactmods.machines.neoforge.machine.block.BoundCompactMachineBlockEntity;
import dev.compactmods.machines.neoforge.machine.block.UnboundCompactMachineEntity;
import dev.compactmods.machines.neoforge.machine.item.BoundCompactMachineItem;
import dev.compactmods.machines.neoforge.machine.item.UnboundCompactMachineItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.Supplier;

@SuppressWarnings("removal")
public interface Machines {
    // TODO: Metal material replacement
    BlockBehaviour.Properties MACHINE_BLOCK_PROPS = BlockBehaviour.Properties
            .of()
            .strength(8.0F, 20.0F)
            .requiresCorrectToolForDrops();

    Supplier<Item.Properties> MACHINE_ITEM_PROPS = Item.Properties::new;

    DeferredBlock<UnboundCompactMachineBlock> UNBOUND_MACHINE_BLOCK = Registries.BLOCKS.register("new_machine", () ->
            new UnboundCompactMachineBlock(MACHINE_BLOCK_PROPS));

    DeferredBlock<BoundCompactMachineBlock> MACHINE_BLOCK = Registries.BLOCKS.register("machine", () ->
            new BoundCompactMachineBlock(MACHINE_BLOCK_PROPS));

    DeferredItem<BoundCompactMachineItem> BOUND_MACHINE_BLOCK_ITEM = Registries.ITEMS.register("machine",
            () -> new BoundCompactMachineItem(MACHINE_ITEM_PROPS.get()));

    DeferredItem<UnboundCompactMachineItem> UNBOUND_MACHINE_BLOCK_ITEM = Registries.ITEMS.register("new_machine",
            () -> new UnboundCompactMachineItem(MACHINE_ITEM_PROPS.get()));

    DeferredHolder<BlockEntityType<?>, BlockEntityType<UnboundCompactMachineEntity>> UNBOUND_MACHINE_ENTITY = Registries.BLOCK_ENTITIES.register(MachineConstants.UNBOUND_MACHINE_ENTITY.getPath(), () ->
            BlockEntityType.Builder.of(UnboundCompactMachineEntity::new, UNBOUND_MACHINE_BLOCK.get())
                    .build(null));

    DeferredHolder<BlockEntityType<?>,BlockEntityType<BoundCompactMachineBlockEntity>> MACHINE_ENTITY = Registries.BLOCK_ENTITIES.register(MachineConstants.BOUND_MACHINE_ENTITY.getPath(), () ->
            BlockEntityType.Builder.of(BoundCompactMachineBlockEntity::new, MACHINE_BLOCK.get())
                    .build(null));

    static void prepare() {

    }
}
