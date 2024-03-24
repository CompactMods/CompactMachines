package dev.compactmods.machines.datagen;

import dev.compactmods.machines.neoforge.data.functions.CopyRoomBindingFunction;
import dev.compactmods.machines.neoforge.machine.Machines;
import dev.compactmods.machines.neoforge.room.Rooms;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.ExplosionCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.function.BiConsumer;

public class BlockLootGenerator implements LootTableSubProvider {

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
        biConsumer.accept(Rooms.BLOCK_BREAKABLE_WALL.getId(), LootTable.lootTable().withPool(LootPool
                .lootPool()
                .name(Rooms.BLOCK_BREAKABLE_WALL.getId().toString())
                .setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion())
                .add(LootItem.lootTableItem(Rooms.ITEM_BREAKABLE_WALL.get()))));

        // Compact Machines
        biConsumer.accept(Machines.MACHINE_BLOCK.getId(), LootTable.lootTable().withPool(LootPool.lootPool()
                .name(Machines.MACHINE_BLOCK.getId().toString())
                .setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion())
                .apply(CopyRoomBindingFunction::new)
                .add(LootItem.lootTableItem(Machines.BOUND_MACHINE_BLOCK_ITEM.get()))));
    }
}
