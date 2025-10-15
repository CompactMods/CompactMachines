package dev.compactmods.machines.util.item;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.ImmutableIntArray;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record ItemHandlerScan(Set<Integer> stacking, Set<Integer> empty) {

    public static ItemHandlerScan scanInventory(ResourceHandler<ItemResource> inventory, ItemStack stack) {
        var stackingBuilder = ImmutableSet.<Integer>builder();
        var emptyBuilder = ImmutableSet.<Integer>builder();
        for(int i = 0; i < inventory.size(); i++) {
            var inSlot = inventory.getResource(i);
            if(inSlot.isEmpty()) {
                emptyBuilder.add(i);
                continue;
            }

            if(inSlot.getMaxStackSize() == 1)
                continue;

            if(!Objects.equals(inSlot.getComponents(), stack.getComponents()))
                continue;

            stackingBuilder.add(i);
        }

        return new ItemHandlerScan(stackingBuilder.build(), emptyBuilder.build());
    }

    public boolean hasSpaceAvailable() {
        return !stacking.isEmpty() || !empty.isEmpty();
    }
}
