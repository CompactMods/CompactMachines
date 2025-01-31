package dev.compactmods.machines.datagen.base.loot;

import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.Rooms;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.Collections;
import java.util.Set;

public class BlockLootGenerator extends BlockLootSubProvider {

    public BlockLootGenerator(HolderLookup.Provider holderLookup) {
        super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags(), holderLookup);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Set.of(Rooms.Blocks.BREAKABLE_WALL.get(),
            Machines.Blocks.BOUND_MACHINE.get(),
            Machines.Blocks.UNBOUND_MACHINE.get());
    }

    @Override
    protected void generate() {
        this.add(Rooms.Blocks.BREAKABLE_WALL.get(), LootTable.lootTable().withPool(LootPool
                .lootPool()
                .name(Rooms.Blocks.BREAKABLE_WALL.getId().toString())
                .setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion())
                .add(LootItem.lootTableItem(Rooms.Items.BREAKABLE_WALL.get()))));

        this.add(Machines.Blocks.UNBOUND_MACHINE.get(), LootTable.lootTable().withPool(LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1))
            .when(ExplosionCondition.survivesExplosion())
            .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                .include(CMDataComponents.MACHINE_COLOR.get())
                .include(CMDataComponents.ROOM_TEMPLATE_ID.get()))
            .add(LootItem.lootTableItem(Machines.Items.UNBOUND_MACHINE.get()))));

        this.add(Machines.Blocks.BOUND_MACHINE.get(), LootTable.lootTable().withPool(LootPool.lootPool()
            .setRolls(ConstantValue.exactly(1))
            .when(ExplosionCondition.survivesExplosion())
            .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)
                .include(CMDataComponents.MACHINE_COLOR.get())
                .include(CMDataComponents.BOUND_ROOM_CODE.get()))
            .add(LootItem.lootTableItem(Machines.Items.BOUND_MACHINE.get()))));
    }
}
