package dev.compactmods.machines.test.junit.asserts;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.junit.jupiter.api.Assertions;

import java.util.List;

public class ItemStackAssertions {

    public static void assertSlotEmpty(IItemHandler handler, int index) {
        final var actual = handler.getStackInSlot(index);
        Assertions.assertEquals(ItemStack.EMPTY, actual, "Expected slot %s to be empty; got %s.".formatted(index, actual));
    }

    public static void assertSlot(IItemHandler handler, int index, ItemStack expected) {
        final var actual = handler.getStackInSlot(index);
        Assertions.assertTrue(ItemStack.matches(expected, actual), "Expected slot %s to be equal to expected stack; got %s.".formatted(index, actual));
    }

    public static void assertListIndex(List<ItemStack> items, int index, ItemStack expected) {
        final var actual = items.get(index);
        Assertions.assertTrue(ItemStack.matches(expected, actual), "Expected slot %s to be equal to expected stack; got %s.".formatted(index, actual));
    }
}
