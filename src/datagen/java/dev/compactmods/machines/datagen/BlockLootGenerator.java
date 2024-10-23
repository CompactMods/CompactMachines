package dev.compactmods.machines.datagen;

import com.google.common.collect.ImmutableList;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.data.CopyRoomBindingFunction;
import dev.compactmods.machines.wall.Walls;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.RegistryObject;

import java.util.Collections;

public class BlockLootGenerator extends BlockLootSubProvider {

    public BlockLootGenerator() {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        this.add(Walls.BLOCK_BREAKABLE_WALL.get(), LootTable.lootTable().withPool(LootPool
                .lootPool()
                .name(Walls.BLOCK_BREAKABLE_WALL.getId().toString())
                .setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion())
                .add(LootItem.lootTableItem(Walls.ITEM_BREAKABLE_WALL.get()))));

        // Compact Machines
        registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_TINY, Machines.MACHINE_BLOCK_ITEM_TINY);
        registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_SMALL, Machines.MACHINE_BLOCK_ITEM_SMALL);
        registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_NORMAL, Machines.MACHINE_BLOCK_ITEM_NORMAL);
        registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_LARGE, Machines.MACHINE_BLOCK_ITEM_LARGE);
        registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_GIANT, Machines.MACHINE_BLOCK_ITEM_GIANT);
        registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_MAXIMUM, Machines.MACHINE_BLOCK_ITEM_MAXIMUM);
    }

    private void registerCompactMachineBlockDrops(RegistryObject<Block> block, RegistryObject<Item> item) {
        LootPool.Builder builder = LootPool.lootPool()
                .name(block.getId().toString())
                .setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion())
                .apply(CopyRoomBindingFunction.binding())
                .add(LootItem.lootTableItem(item.get()));

        this.add(block.get(), LootTable.lootTable().withPool(builder));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ImmutableList.of(
                // Breakable Walls
                Walls.BLOCK_BREAKABLE_WALL.get(),

                // Compact Machines
                Machines.MACHINE_BLOCK_TINY.get(),
                Machines.MACHINE_BLOCK_SMALL.get(),
                Machines.MACHINE_BLOCK_NORMAL.get(),
                Machines.MACHINE_BLOCK_LARGE.get(),
                Machines.MACHINE_BLOCK_GIANT.get(),
                Machines.MACHINE_BLOCK_MAXIMUM.get()
        );
    }
}
