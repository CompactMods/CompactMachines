package dev.compactmods.machines.datagen;

import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;

public class BlockLootGenerator implements LootTableSubProvider {

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> biConsumer) {
//        biConsumer.accept(Walls.BLOCK_BREAKABLE_WALL.getId(), LootTable.lootTable().withPool(LootPool
//                .lootPool()
//                .name(Walls.BLOCK_BREAKABLE_WALL.getId().toString())
//                .setRolls(ConstantValue.exactly(1))
//                .when(ExplosionCondition.survivesExplosion())
//                .add(LootItem.lootTableItem(Walls.ITEM_BREAKABLE_WALL.get()))));

        // Compact Machines
//        biConsumer.accept(Machines.MACHINE_BLOCK.getId(), LootTable.lootTable().withPool(LootPool.lootPool()
//                .name(Machines.MACHINE_BLOCK.getId().toString())
//                .setRolls(ConstantValue.exactly(1))
//                .when(ExplosionCondition.survivesExplosion())
//                .apply(CopyRoomBindingFunction::new)
//                .add(LootItem.lootTableItem(Machines.BOUND_MACHINE_BLOCK_ITEM.get()))));
    }
}
