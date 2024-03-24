package dev.compactmods.machines.neoforge.room.upgrade;

import dev.compactmods.machines.api.room.upgrade.IRoomUpgradeInventory;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public class NeoforgeRoomUpgradeInventory extends ItemStackHandler implements IRoomUpgradeInventory {

    public static final NeoforgeRoomUpgradeInventory EMPTY = new NeoforgeRoomUpgradeInventory();

    public NeoforgeRoomUpgradeInventory() {
        super(9);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return !stack.isEmpty() && stack.is(RoomUpgrade.ITEM_TAG);
    }
}
