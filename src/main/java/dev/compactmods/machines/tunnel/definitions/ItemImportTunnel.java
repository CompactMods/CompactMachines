package dev.compactmods.machines.tunnel.definitions;

import dev.compactmods.machines.api.tunnels.ITunnel;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.capability.ICapabilityTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.ITunnelTeardown;
import dev.compactmods.machines.api.tunnels.lifecycle.TeardownReason;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ItemImportTunnel extends TunnelDefinition
        implements ITunnelTeardown<ItemImportTunnel.Instance>,
            ICapabilityTunnel<ItemImportTunnel.Instance> {

    @Override
    public int getTunnelRingColor() {
        return 0xffcd8f24;
    }

    @Override
    public int getTunnelIndicatorColor() {
        return IMPORT_COLOR;
    }

    /**
     * Handle initialization tasks for the tunnel's data here.
     *
     * @param tunnel The location of the new tunnel being created.
     */
    @Override
    public ITunnel newInstance(BlockPos pos, Direction side) {
        return new Instance(side);
    }

    @Override
    public void teardown(Instance instance, TeardownReason reason) {
//        instance.room.getCapabilityManager()
//                .removeCapability(this, CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, instance.side);
    }

    /**
     * Fetch a capability instance from a tunnel.
     *
     * @param capType Capability type. See implementations like {@link IItemHandler} as a reference.
     * @return LazyOptional instance of the capability, or LO.empty otherwise.
     */
    @Override
    public <CapType> LazyOptional<CapType> getCapability(Capability<CapType> capType, Instance instance) {
        if(capType == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return instance.lazy();
        }

        return LazyOptional.empty();
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

    public static class Instance implements ITunnel {

        final Direction side;
        private final LazyOptional<InfiniteItemSource> laze;
        InfiniteItemSource items = new InfiniteItemSource(Items.COBBLESTONE);

        public Instance(Direction side) {
            this.side = side;
            this.laze = LazyOptional.of(this::getItems);
        }

        private InfiniteItemSource getItems() {
            return items;
        }

        public <CapType> LazyOptional<CapType> lazy() {
            return laze.cast();
        }
    }
}
