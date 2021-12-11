package dev.compactmods.machines.datagen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.reference.Reference;
import net.minecraft.world.level.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.functions.CopyNbtFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.providers.nbt.ContextNbtProvider;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.registries.RegistryObject;

public class BlockLootGenerator extends LootTableProvider {

    public BlockLootGenerator(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(Pair.of(Blocks::new, LootContextParamSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        map.forEach((name, table) -> LootTables.validate(validationtracker, name, table));
    }

    private static class Blocks extends BlockLoot {
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
                    .setRolls(ConstantValue.exactly(1))
                    .when(ExplosionCondition.survivesExplosion())
                    .add(LootItem.lootTableItem(item.get()));

            this.add(block.get(), LootTable.lootTable().withPool(builder));
            return builder;
        }

        private final LootItemFunction.Builder CopyOwnerAndReferenceFunction = CopyNbtFunction.copyData(ContextNbtProvider.BLOCK_ENTITY)
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
                    .setRolls(ConstantValue.exactly(1))
                    .when(ExplosionCondition.survivesExplosion())
                    .apply(CopyOwnerAndReferenceFunction)
                    .add(LootItem.lootTableItem(item.get()));

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
