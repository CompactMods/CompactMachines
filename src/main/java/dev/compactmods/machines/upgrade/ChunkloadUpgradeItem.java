package dev.compactmods.machines.upgrade;

import dev.compactmods.machines.api.upgrade.RoomUpgradeHelper;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ChunkloadUpgradeItem extends RoomUpgradeItem {
    public ChunkloadUpgradeItem(Properties props) {
        super(props);
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        final var stack = new ItemStack(MachineRoomUpgrades.CHUNKLOADER.get(), 1);
        final var info = stack.getOrCreateTagElement(RoomUpgradeHelper.NBT_UPGRADE_NODE);
        info.putString(RoomUpgradeHelper.NBT_UPDATE_ID, ChunkloadUpgrade.REG_ID.toString());

        stacks.add(stack);
    }
}
