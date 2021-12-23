package dev.compactmods.machines.tunnel;

import dev.compactmods.machines.api.room.IMachineRoom;
import dev.compactmods.machines.api.tunnels.ITunnelPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.connection.ITunnelConnection;
import dev.compactmods.machines.api.tunnels.item.IItemImportTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.TeardownReason;
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

    @Override
    public void setup(IMachineRoom room, ITunnelPosition tunnel, ITunnelConnection added) {
        room.getTunnels().register(this, tunnel.pos());

        room.getCapabilityManager()
                .addCapability(this, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, new InfiniteItemSource(Items.COBBLESTONE), tunnel.side());
    }

    @Override
    public void teardown(IMachineRoom room, ITunnelPosition position, ITunnelConnection removed, TeardownReason reason) {
        if(reason == TeardownReason.REMOVED)
            room.getTunnels().unregister(position.pos());

        room.getCapabilityManager()
                .removeCapability(this, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, removed.side());
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
