package com.robotgryphon.compactmachines.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.robotgryphon.compactmachines.core.Registrations;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Item;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BlockLootGenerator extends LootTableProvider {

    public BlockLootGenerator(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
        return ImmutableList.of(Pair.of(Blocks::new, LootParameterSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
        map.forEach((name, table) -> LootTableManager.func_227508_a_(validationtracker, name, table));
    }

    private static class Blocks extends BlockLootTables {
        @Override
        protected void addTables() {
            registerSelfDroppedBlock(Registrations.BLOCK_BREAKABLE_WALL, Registrations.ITEM_BREAKABLE_WALL);

            // Compact Machines
            registerSelfDroppedBlock(Registrations.MACHINE_BLOCK_TINY, Registrations.MACHINE_BLOCK_ITEM_TINY);
            registerSelfDroppedBlock(Registrations.MACHINE_BLOCK_SMALL, Registrations.MACHINE_BLOCK_ITEM_SMALL);
            registerSelfDroppedBlock(Registrations.MACHINE_BLOCK_NORMAL, Registrations.MACHINE_BLOCK_ITEM_NORMAL);
            registerSelfDroppedBlock(Registrations.MACHINE_BLOCK_LARGE, Registrations.MACHINE_BLOCK_ITEM_LARGE);
            registerSelfDroppedBlock(Registrations.MACHINE_BLOCK_GIANT, Registrations.MACHINE_BLOCK_ITEM_GIANT);
            registerSelfDroppedBlock(Registrations.MACHINE_BLOCK_MAXIMUM, Registrations.MACHINE_BLOCK_ITEM_MAXIMUM);
        }

        private LootPool.Builder registerSelfDroppedBlock(RegistryObject<Block> block, RegistryObject<Item> item) {
            LootPool.Builder builder = LootPool.builder()
                    .name(block.get().getRegistryName().toString())
                    .rolls(ConstantRange.of(1))
                    .acceptCondition(SurvivesExplosion.builder())
                    .addEntry(ItemLootEntry.builder(item.get()));

            this.registerLootTable(block.get(), LootTable.builder().addLootPool(builder));
            return builder;
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ImmutableList.of(
                    // Breakable Walls
                    Registrations.BLOCK_BREAKABLE_WALL.get(),

                    // Compact Machines
                    Registrations.MACHINE_BLOCK_TINY.get(),
                    Registrations.MACHINE_BLOCK_SMALL.get(),
                    Registrations.MACHINE_BLOCK_NORMAL.get(),
                    Registrations.MACHINE_BLOCK_LARGE.get(),
                    Registrations.MACHINE_BLOCK_GIANT.get(),
                    Registrations.MACHINE_BLOCK_MAXIMUM.get()
                );
        }
    }
}
