package dev.compactmods.machines.util.item;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class ItemHandlerUtil {

    public static List<ItemStack> insertMultipleStacks(IItemHandler itemHandler, List<ItemStack> drops) {
        Deque<ItemStack> remainingDrops = new ArrayDeque<>(drops);
        final var failedToPush = ImmutableList.<ItemStack>builder();

        while (!remainingDrops.isEmpty()) {
            var workingStack = remainingDrops.pop();
            var scan = ItemHandlerScan.scanInventory(itemHandler, workingStack);
            if (!scan.hasSpaceAvailable()) {
                failedToPush.add(workingStack);
                continue;
            }

            // Try stackable slots first, then empty
            var stackableSlots = new ArrayDeque<Integer>(scan.stacking());
            stackableSlots.addAll(scan.empty());

            boolean doneWithStack = false;
            while(!doneWithStack) {
                // No more space
                if(stackableSlots.isEmpty()) {
                    failedToPush.addAll(remainingDrops);
                    remainingDrops.clear();
                    break;
                }

                var working = stackableSlots.peek();
                workingStack = itemHandler.insertItem(working, workingStack, false);
                if (workingStack.isEmpty()) {
                    doneWithStack = true;
                    break;
                }

                stackableSlots.pop();
                if(stackableSlots.isEmpty()) {
                    failedToPush.add(workingStack.copy());
                    doneWithStack = true;
                }
            }
        }

        return failedToPush.build();
    }
}
