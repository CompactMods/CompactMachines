package com.robotgryphon.compactmachines.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.robotgryphon.compactmachines.core.Registrations;
import com.robotgryphon.compactmachines.reference.Reference;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Item;
import net.minecraft.loot.*;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.functions.CopyNbt;
import net.minecraft.loot.functions.ILootFunction;
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
        map.forEach((name, table) -> LootTableManager.validateLootTable(validationtracker, name, table));
    }

    private static class Blocks extends BlockLootTables {
        @Override
        protected void addTables() {
            registerSelfDroppedBlock(Registrations.BLOCK_BREAKABLE_WALL, Registrations.ITEM_BREAKABLE_WALL);

            // Compact Machines
            registerCompactMachineBlockDrops(Registrations.MACHINE_BLOCK_TINY, Registrations.MACHINE_BLOCK_ITEM_TINY);
            registerCompactMachineBlockDrops(Registrations.MACHINE_BLOCK_SMALL, Registrations.MACHINE_BLOCK_ITEM_SMALL);
            registerCompactMachineBlockDrops(Registrations.MACHINE_BLOCK_NORMAL, Registrations.MACHINE_BLOCK_ITEM_NORMAL);
            registerCompactMachineBlockDrops(Registrations.MACHINE_BLOCK_LARGE, Registrations.MACHINE_BLOCK_ITEM_LARGE);
            registerCompactMachineBlockDrops(Registrations.MACHINE_BLOCK_GIANT, Registrations.MACHINE_BLOCK_ITEM_GIANT);
            registerCompactMachineBlockDrops(Registrations.MACHINE_BLOCK_MAXIMUM, Registrations.MACHINE_BLOCK_ITEM_MAXIMUM);
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

        private ILootFunction.IBuilder CopyOwnerAndReferenceFunction = CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY)
                .replaceOperation(Reference.CompactMachines.OWNER_NBT, Reference.CompactMachines.OWNER_NBT)
                .replaceOperation("coords", "cm.coords");

        private LootPool.Builder registerCompactMachineBlockDrops(RegistryObject<Block> block, RegistryObject<Item> item) {
//            LootTable.builder()
//                    .addLootPool(withSurvivesExplosion(shulker, LootPool.builder()
//                            .rolls(ConstantRange.of(1))
//                            .addEntry(ItemLootEntry.builder(shulker)
//                                    .acceptFunction(CopyName.builder(CopyName.Source.BLOCK_ENTITY))
//                                    .acceptFunction(CopyNbt.builder(CopyNbt.Source.BLOCK_ENTITY)
//                                            .replaceOperation("Lock", "BlockEntityTag.Lock")
//                                            .replaceOperation("LootTable", "BlockEntityTag.LootTable")
//                                            .replaceOperation("LootTableSeed", "BlockEntityTag.LootTableSeed"))
//                                    .acceptFunction(SetContents.builderIn().addLootEntry(DynamicLootEntry.func_216162_a(ShulkerBoxBlock.CONTENTS))))));

            LootPool.Builder builder = LootPool.builder()
                    .name(block.get().getRegistryName().toString())
                    .rolls(ConstantRange.of(1))
                    .acceptCondition(SurvivesExplosion.builder())
                    .acceptFunction(CopyOwnerAndReferenceFunction)
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
