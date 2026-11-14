package dev.compactmods.machines.datagen.base.loot;

import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;

import java.util.Collections;
import java.util.Set;

public class BlockLootGenerator extends BlockLootSubProvider {

    public BlockLootGenerator(LootTableSubProvider.Context output) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), output);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Set.of(Rooms.Blocks.BREAKABLE_WALL.get(),
            Machines.Blocks.MACHINE.get());
    }

    @Override
    protected void generate() {
        this.add(Rooms.Blocks.BREAKABLE_WALL.get(), LootTable.lootTable().withPool(LootPool
                .lootPool()
                .name(Rooms.Blocks.BREAKABLE_WALL.getId().toString())
                .setRolls(ContextIntProviders.exactly(1))
                .when(ExplosionCondition.survivesExplosion())
                .add(LootItem.lootTableItem(Rooms.Items.BREAKABLE_WALL.get()))));

        this.add(Machines.Blocks.MACHINE.get(), LootTable.lootTable().withPool(LootPool.lootPool()
            .setRolls(ContextIntProviders.exactly(1))
            .when(ExplosionCondition.survivesExplosion())
            .apply(CopyComponentsFunction.copyComponentsFromBlockEntity(LootContextParams.BLOCK_ENTITY)
                .include(DataComponents.CUSTOM_NAME)
                .include(Machines.DataComponents.MACHINE_COLOR.get())
                .include(Rooms.DataComponents.BOUND_ROOM_CODE.get()))
            .add(LootItem.lootTableItem(Machines.Items.MACHINE.get()))));
    }
}
