package dev.compactmods.machines.forgebuiltin.tunnel.definitions;

import com.google.common.collect.ImmutableSet;
import dev.compactmods.machines.api.tunnels.TunnelDefinition;
import dev.compactmods.machines.api.tunnels.capability.CapabilityTunnel;
import dev.compactmods.machines.api.tunnels.lifecycle.IPlayerLifecycleEventReason;
import dev.compactmods.machines.api.tunnels.lifecycle.TunnelInstance;
import dev.compactmods.machines.api.tunnels.lifecycle.removal.ITunnelRemoveEventListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.FastColor;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class BufferedItemTunnel implements TunnelDefinition, CapabilityTunnel<BufferedItemTunnel.Instance>,
        ITunnelRemoveEventListener<BufferedItemTunnel.Instance> {

    @Override
    public int ringColor() {
        return FastColor.ARGB32.color(255, 205, 143, 36);
    }

    /**
     * Handle initialization tasks for the tunnel's data here.
     */
    public Instance newInstance(BlockPos pos, Direction side) {
        return new Instance(10);
    }

    @Override
    public ImmutableSet<Capability<?>> getSupportedCapabilities() {
        return ImmutableSet.of(ForgeCapabilities.ITEM_HANDLER);
    }

    /**
     * Fetch a capability instance from a tunnel.
     *
     * @param capType Capability type. See implementations like {@link IItemHandler} as a reference.
     * @return LazyOptional instance of the capability, or LO.empty otherwise.
     */
    @Override
    public <CapType> LazyOptional<CapType> getCapability(Capability<CapType> capType, Instance instance) {
        if (capType == ForgeCapabilities.ITEM_HANDLER) {
            return instance.lazy();
        }

        return LazyOptional.empty();
    }

    @Override
    public @Nullable BeforeRemove createBeforeRemoveHandler(Instance instance) {
        // Only allow players to remove item tunnels
        return (server, position, reason) -> reason instanceof IPlayerLifecycleEventReason;
    }

    /**
     * Drops items into the machine room before the tunnel is removed from the wall.
     *
     * @param instance The tunnel instance being modified.
     */
    @Override
    public @Nullable AfterRemove createAfterRemoveHandler(Instance instance) {
        return (server, position, reason) -> {
            if(reason instanceof IPlayerLifecycleEventReason playerReason) {
                var player = playerReason.player();
                var dropAt = player.position();

                NonNullList<ItemStack> stacks = NonNullList.create();
                for (int i = 0; i < instance.handler.getSlots(); i++) {
                    ItemStack stack = instance.handler.getStackInSlot(i);
                    if (!stack.isEmpty())
                        stacks.add(stack);
                }

                stacks.forEach(s -> Containers.dropItemStack(player.level, dropAt.x, dropAt.y + 0.5, dropAt.z, s));
            }
        };
    }

    public static class Instance implements TunnelInstance, INBTSerializable<CompoundTag> {

        private final LazyOptional<IItemHandler> laze;
        final ItemStackHandler handler;

        public Instance(int buffer) {
            this.handler = new ItemStackHandler(buffer);
            this.laze = LazyOptional.of(this::getItems);
        }

        private @Nonnull
        IItemHandler getItems() {
            return handler;
        }

        public <CapType> LazyOptional<CapType> lazy() {
            return laze.cast();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.put("items", handler.serializeNBT());
            return tag;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            handler.deserializeNBT(nbt.getCompound("items"));
        }
    }
}
