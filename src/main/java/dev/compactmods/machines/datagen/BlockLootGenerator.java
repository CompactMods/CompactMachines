package dev.compactmods.machines.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.reference.Reference;
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
        map.forEach((name, table) -> LootTableManager.validate(validationtracker, name, table));
    }

    private static class Blocks extends BlockLootTables {
        @Override
        protected void addTables() {
            registerSelfDroppedBlock(Registration.BLOCK_BREAKABLE_WALL, Registration.ITEM_BREAKABLE_WALL);

            // Compact Machines
            registerCompactMachineBlockDrops(Registration.MACHINE_BLOCK_TINY, Registration.MACHINE_BLOCK_ITEM_TINY);
            registerCompactMachineBlockDrops(Registration.MACHINE_BLOCK_SMALL, Registration.MACHINE_BLOCK_ITEM_SMALL);
            registerCompactMachineBlockDrops(Registration.MACHINE_BLOCK_NORMAL, Registration.MACHINE_BLOCK_ITEM_NORMAL);
            registerCompactMachineBlockDrops(Registration.MACHINE_BLOCK_LARGE, Registration.MACHINE_BLOCK_ITEM_LARGE);
            registerCompactMachineBlockDrops(Registration.MACHINE_BLOCK_GIANT, Registration.MACHINE_BLOCK_ITEM_GIANT);
            registerCompactMachineBlockDrops(Registration.MACHINE_BLOCK_MAXIMUM, Registration.MACHINE_BLOCK_ITEM_MAXIMUM);
        }

        private LootPool.Builder registerSelfDroppedBlock(RegistryObject<Block> block, RegistryObject<Item> item) {
            LootPool.Builder builder = LootPool.lootPool()
                    .name(block.get().getRegistryName().toString())
                    .setRolls(ConstantRange.exactly(1))
                    .when(SurvivesExplosion.survivesExplosion())
                    .add(ItemLootEntry.lootTableItem(item.get()));

            this.add(block.get(), LootTable.lootTable().withPool(builder));
            return builder;
        }

        private ILootFunction.IBuilder CopyOwnerAndReferenceFunction = CopyNbt.copyData(CopyNbt.Source.BLOCK_ENTITY)
                .copy(Reference.CompactMachines.OWNER_NBT, Reference.CompactMachines.OWNER_NBT)
                .copy("coords", "cm.coords");

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
//                                    .acceptFunction(SetContents.builderIn().addLootEntry(DynamicLootEntry.dynamicEntry(ShulkerBoxBlock.CONTENTS))))));

            LootPool.Builder builder = LootPool.lootPool()
                    .name(block.get().getRegistryName().toString())
                    .setRolls(ConstantRange.exactly(1))
                    .when(SurvivesExplosion.survivesExplosion())
                    .apply(CopyOwnerAndReferenceFunction)
                    .add(ItemLootEntry.lootTableItem(item.get()));

            this.add(block.get(), LootTable.lootTable().withPool(builder));
            return builder;
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ImmutableList.of(
                    // Breakable Walls
                    Registration.BLOCK_BREAKABLE_WALL.get(),

                    // Compact Machines
                    Registration.MACHINE_BLOCK_TINY.get(),
                    Registration.MACHINE_BLOCK_SMALL.get(),
                    Registration.MACHINE_BLOCK_NORMAL.get(),
                    Registration.MACHINE_BLOCK_LARGE.get(),
                    Registration.MACHINE_BLOCK_GIANT.get(),
                    Registration.MACHINE_BLOCK_MAXIMUM.get()
                );
        }
    }
}
