package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.api.tunnels.item.IItemImportTunnel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ItemImportTunnel extends TunnelDefinition implements IItemImportTunnel {
    @Override
    public int getTunnelRingColor() {
        return 0xffcd8f24;
    }

    @Override
    public int getTunnelIndicatorColor() {
        return IMPORT_COLOR;
    }

    /**
     * Handle initialization tasks for the tunnel's capabilities here.
     *
     * @param room  The room the tunnel was added to.
     * @param added The connection being set up from room to machine.
     */
    @Override
    public void setupCapabilities(IMachineRoom room, ITunnelConnection added) {
        var pos = room.getChunk();
        var level = room.getLevel();

        room.getCapabilities().addCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new InfiniteItemSource(Items.COBBLESTONE), added.side());
    }

    /**
     * Handle teardown of tunnel capabilities here.
     *
     * @param room    The room the tunnel was removed from.
     * @param removed The connection being torn down, from machine to room.
     */
    @Override
    public void teardownCapabilities(IMachineRoom room, ITunnelConnection removed) {
        room.getCapabilities().removeCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, removed.side());
    }

    private static class InfiniteItemSource extends ItemStackHandler {
        private final Item item;

        public InfiniteItemSource(Item item) {
            super(1);
            this.item = item;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return new ItemStack(item, amount);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return stack;
        }
    }
}
