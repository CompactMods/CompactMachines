package dev.compactmods.machines.test.junit;

import dev.compactmods.machines.test.junit.asserts.ItemStackAssertions;
import dev.compactmods.machines.util.item.ItemHandlerUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

@ExtendWith(EphemeralTestServerProvider.class)
public class ItemHandlingTests {

    private static ItemStack sticks(int size) {
        return new ItemStack(Items.STICK, size);
    }

    private static ItemStack logs(int size) {
        return new ItemStack(Items.OAK_LOG, size);
    }

    private static List<ItemStack> getTreeDrops() {
        return List.of(logs(5), sticks(2));
    }

    @Test
    public void fillsFirstAvailableEmptySlots() {
        var drops = getTreeDrops();
        var fakeChest = new ItemStackHandler(5);

        var failed = ItemHandlerUtil.insertMultipleStacks(fakeChest, drops);

        // Failed = Empty
        // Oak Logs = Slot 0 (took empty slot)
        // Sticks = Slot 1 (took empty slot)
        Assertions.assertEquals(0, failed.size());

        ItemStackAssertions.assertSlot(fakeChest, 0, logs(5));
        ItemStackAssertions.assertSlot(fakeChest, 1, sticks(2));

        ItemStackAssertions.assertSlotEmpty(fakeChest, 2);
        ItemStackAssertions.assertSlotEmpty(fakeChest, 3);
        ItemStackAssertions.assertSlotEmpty(fakeChest, 4);
    }

    @Test
    public void stacksOntoExistingSlotsFirst() {
        var drops = getTreeDrops();
        var fakeChest = new ItemStackHandler(5);
        fakeChest.insertItem(1, new ItemStack(Items.STICK, 1), false);
        fakeChest.insertItem(2, new ItemStack(Items.OAK_LOG, 1), false);
        var failed = ItemHandlerUtil.insertMultipleStacks(fakeChest, drops);

        // Failed = Empty
        // Oak Logs = Slot 2 (stacked on existing)
        // Sticks = Slot 1 (stacked on existing)
        Assertions.assertEquals(0, failed.size());

        ItemStackAssertions.assertSlot(fakeChest, 0, ItemStack.EMPTY);
        ItemStackAssertions.assertSlot(fakeChest, 1, sticks(3));
        ItemStackAssertions.assertSlot(fakeChest, 2, logs(6));
        ItemStackAssertions.assertSlot(fakeChest, 3, ItemStack.EMPTY);
        ItemStackAssertions.assertSlot(fakeChest, 4, ItemStack.EMPTY);
    }

    @Test
    public void overflowFillsFromFirstEmptySlot() {
        var drops = getTreeDrops();
        var fakeChest = new ItemStackHandler(5);
        fakeChest.insertItem(3, new ItemStack(Items.STICK, 63), false);
        var failed = ItemHandlerUtil.insertMultipleStacks(fakeChest, drops);

        // Failed = Empty
        // Oak Logs = Slot 0 (took empty slot)
        // Sticks = Slots 1 + 3 (stacked on existing - 3 should be full, 4 should be empty)
        Assertions.assertEquals(0, failed.size());

        ItemStackAssertions.assertSlot(fakeChest, 0, logs(5));
        ItemStackAssertions.assertSlot(fakeChest, 1, sticks(1));
        ItemStackAssertions.assertSlot(fakeChest, 2, ItemStack.EMPTY);
        ItemStackAssertions.assertSlot(fakeChest, 3, sticks(64));
        ItemStackAssertions.assertSlot(fakeChest, 4, ItemStack.EMPTY);
    }

    @Test
    public void stacksExistingAndFillsFirstEmptySlots() {
        var drops = getTreeDrops();
        var fakeChest = new ItemStackHandler(5);
        fakeChest.insertItem(2, new ItemStack(Items.OAK_LOG, 1), false);

        var failed = ItemHandlerUtil.insertMultipleStacks(fakeChest, drops);

        // Failed = Empty
        // Oak Logs = Slot 2 (stacked on existing)
        // Sticks = Slot 0 (took empty slot)
        Assertions.assertEquals(0, failed.size());

        ItemStackAssertions.assertSlot(fakeChest, 0, sticks(2));
        ItemStackAssertions.assertSlot(fakeChest, 1, ItemStack.EMPTY);
        ItemStackAssertions.assertSlot(fakeChest, 2, logs(6));
        ItemStackAssertions.assertSlot(fakeChest, 3, ItemStack.EMPTY);
        ItemStackAssertions.assertSlot(fakeChest, 4, ItemStack.EMPTY);
    }

    @Test
    public void doesNotOverfill() {
        var drops = getTreeDrops();
        var fakeChest = new ItemStackHandler(1);
        fakeChest.insertItem(0, new ItemStack(Items.OAK_LOG, 64), false);

        var failed = ItemHandlerUtil.insertMultipleStacks(fakeChest, drops);

        // Failed = 2 Stacks
        Assertions.assertEquals(2, failed.size());

        ItemStackAssertions.assertListIndex(failed, 0, logs(5));
        ItemStackAssertions.assertListIndex(failed, 1, sticks(2));
    }
}
