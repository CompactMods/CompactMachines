package dev.compactmods.machines.upgrade;

<<<<<<< HEAD
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.upgrade.RoomUpgradeHelper;
=======
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import dev.compactmods.machines.api.upgrade.RoomUpgradeHelper;
import net.minecraft.client.Minecraft;
>>>>>>> ccf34df... Test
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ChunkloadUpgradeItem extends RoomUpgradeItem {
    public ChunkloadUpgradeItem(Properties props) {
        super(props);
    }

<<<<<<< HEAD
    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
=======



    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        if (tab != CompactMachines.COMPACT_MACHINES_ITEMS) return;

>>>>>>> ccf34df... Test
        final var stack = new ItemStack(MachineRoomUpgrades.CHUNKLOADER.get(), 1);
        final var info = stack.getOrCreateTagElement(RoomUpgradeHelper.NBT_UPGRADE_NODE);
        info.putString(RoomUpgradeHelper.NBT_UPDATE_ID, ChunkloadUpgrade.REG_ID.toString());

        stacks.add(stack);
    }

    @Override
    public RoomUpgrade getUpgradeType() {
        return MachineRoomUpgrades.CHUNKLOAD.get();
    }
}
