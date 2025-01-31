package dev.compactmods.machines.api.room.upgrade.inventory;

import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeInstance;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.UUID;
import java.util.stream.Stream;

public class RoomUpgradeInventory extends ItemStackHandler {

    public static final RoomUpgradeInventory EMPTY = new RoomUpgradeInventory();

    public RoomUpgradeInventory() {
        super(9);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.has(CMDataComponents.UPGRADE_LIST_COMPONENT);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    public Stream<ItemStack> items() {
        final Stream.Builder<ItemStack> b = Stream.builder();
        for (int i = 0; i < 9; i++) {
            final var stack = getStackInSlot(i);
            if (!stack.isEmpty()) {
                b.add(stack);
            }
        }

        return b.build();
    }
}
