package dev.compactmods.machines.upgrade;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.upgrade.RoomUpgrade;
import dev.compactmods.machines.Registries;
import dev.compactmods.machines.room.upgrade.RoomUpgradeWorkbench;
import dev.compactmods.machines.room.upgrade.RoomUpgradeWorkbenchEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class MachineRoomUpgrades {

    public static final Supplier<IForgeRegistry<RoomUpgrade>> REGISTRY = Registries.UPGRADES.makeRegistry(RegistryBuilder::new);

    // ================================================================================================================

    public static final RegistryObject<Item> ROOM_UPGRADE = Registries.ITEMS.register("room_upgrade", () -> new RoomUpgradeItem(new Item.Properties()
            .tab(CompactMachines.COMPACT_MACHINES_ITEMS)
            .stacksTo(1)));

    public static final RegistryObject<Block> WORKBENCH_BLOCK = Registries.BLOCKS.register("workbench", () ->
            new RoomUpgradeWorkbench(BlockBehaviour.Properties.of(Material.METAL)
                    .requiresCorrectToolForDrops()
                    .lightLevel(state -> 3)));

    public static final RegistryObject<BlockItem> WORKBENCH_ITEM = Registries.ITEMS.register("workbench", () ->
            new BlockItem(WORKBENCH_BLOCK.get(), new Item.Properties().tab(CompactMachines.COMPACT_MACHINES_ITEMS)));

    public static final RegistryObject<BlockEntityType<RoomUpgradeWorkbenchEntity>> ROOM_UPDATE_ENTITY = Registries.BLOCK_ENTITIES.register(
            "workbench", () -> BlockEntityType.Builder.of(RoomUpgradeWorkbenchEntity::new, WORKBENCH_BLOCK.get())
                    .build(null));

    public static void prepare() {

    }
}
