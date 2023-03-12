package dev.compactmods.machines.forge.data.generated;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import dev.compactmods.machines.forge.machine.Machines;
import dev.compactmods.machines.forge.data.generated.functions.CopyRoomBindingFunction;
import dev.compactmods.machines.forge.wall.Walls;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
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
    protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return ImmutableList.of(Pair.of(Blocks::new, LootContextParamSets.BLOCK));
    }

    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, @Nonnull ValidationContext val) {
        map.forEach((name, table) -> LootTables.validate(val, name, table));
    }

    private static class Blocks extends BlockLoot {
        @Override
        protected void addTables() {
            this.add(Walls.BLOCK_BREAKABLE_WALL.get(), LootTable.lootTable().withPool(LootPool
                    .lootPool()
                    .name(Walls.BLOCK_BREAKABLE_WALL.getId().toString())
                    .setRolls(ConstantValue.exactly(1))
                    .when(ExplosionCondition.survivesExplosion())
                    .add(LootItem.lootTableItem(Walls.ITEM_BREAKABLE_WALL.get()))));

            // Compact Machines
            registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK);

            // Legacy Machines
            registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_TINY);
            registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_SMALL);
            registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_NORMAL);
            registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_LARGE);
            registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_GIANT);
            registerCompactMachineBlockDrops(Machines.MACHINE_BLOCK_MAXIMUM);
        }

        private void registerCompactMachineBlockDrops(RegistryObject<Block> block) {
            LootPool.Builder builder = LootPool.lootPool()
                    .name(block.getId().toString())
                    .setRolls(ConstantValue.exactly(1))
                    .when(ExplosionCondition.survivesExplosion())
                    .apply(CopyRoomBindingFunction.binding())
                    .add(LootItem.lootTableItem(Machines.BOUND_MACHINE_BLOCK_ITEM.get()));

            this.add(block.get(), LootTable.lootTable().withPool(builder));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return ImmutableList.of(
                    // Breakable Walls
                    Walls.BLOCK_BREAKABLE_WALL.get(),

                    // Compact Machines
                    Machines.MACHINE_BLOCK.get(),
                    Machines.MACHINE_BLOCK_TINY.get(),
                    Machines.MACHINE_BLOCK_SMALL.get(),
                    Machines.MACHINE_BLOCK_NORMAL.get(),
                    Machines.MACHINE_BLOCK_LARGE.get(),
                    Machines.MACHINE_BLOCK_GIANT.get(),
                    Machines.MACHINE_BLOCK_MAXIMUM.get()
                );
        }
    }
}
