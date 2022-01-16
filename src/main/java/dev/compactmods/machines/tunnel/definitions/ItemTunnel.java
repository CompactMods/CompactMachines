package dev.compactmods.machines.tunnel.definitions;

import dev.compactmods.machines.api.tunnels.ITunnel;
import dev.compactmods.machines.api.tunnels.ITunnelPosition;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.capability.ICapabilityTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.ITunnelTeardown;
import dev.compactmods.machines.api.tunnels.lifecycle.TeardownReason;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemTunnel extends TunnelDefinition
        implements ICapabilityTunnel<ItemImportStorage>, ITunnelTeardown<ItemImportStorage> {

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
        return new ItemImportStorage(10, side);
    }

    /**
     * Fetch a capability instance from a tunnel.
     *
     * @param capType Capability type. See implementations like {@link IItemHandler} as a reference.
     * @return LazyOptional instance of the capability, or LO.empty otherwise.
     */
    @Override
    public <CapType> LazyOptional<CapType> getCapability(Capability<CapType> capType, ItemImportStorage instance) {
        if(capType == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return instance.lazy();
        }

        return LazyOptional.empty();
    }

    /**
     * Handle teardown of tunnel here.
     *
     * @param instance The tunnel instance being modified.
     * @param reason   The reason the teardown is occurring.
     */
    @Override
    public void teardown(ITunnelPosition position, ItemImportStorage instance, TeardownReason reason) {
        BlockPos dropAt = position.pos().relative(position.side());

        NonNullList<ItemStack> stacks = NonNullList.create();
        for(int i = 0; i < instance.handler.getSlots(); i++) {
            ItemStack stack = instance.handler.getStackInSlot(i);
            if(!stack.isEmpty())
                stacks.add(stack);
        }

        Containers.dropContents(position.level(), dropAt, stacks);
    }
}
