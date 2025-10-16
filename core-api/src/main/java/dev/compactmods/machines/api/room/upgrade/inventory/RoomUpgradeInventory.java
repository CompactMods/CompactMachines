package dev.compactmods.machines.api.room.upgrade.inventory;

import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponent;
import dev.compactmods.machines.api.room.upgrade.event.lifecycle.UpgradeAppliedEventListener;
import dev.compactmods.machines.api.room.upgrade.event.lifecycle.UpgradeRemovedEventListener;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class RoomUpgradeInventory extends ItemStacksResourceHandler {

    private RoomInstance instance;

    public static final StreamCodec<RegistryFriendlyByteBuf, RoomUpgradeInventory> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, x -> x.stacks,
            RoomUpgradeInventory::new
    );

    public RoomUpgradeInventory() {
        super(9);
        this.instance = null;
    }

    public RoomUpgradeInventory(List<ItemStack> bufStacks) {
        super(9);
        this.instance = null;

        for(int slot = 0; slot < bufStacks.size(); slot++) {
            ItemStack stack = bufStacks.get(slot);
            if(!stack.isEmpty()) {
                this.stacks.set(slot, stack);
            }
        }
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return !resource.isEmpty() && resource.has(CMDataComponents.UPGRADE_LIST_COMPONENT);
    }

    @Override
    protected int getCapacity(int index, @NotNull ItemResource resource) {
        return 1;
    }

    public Stream<ItemStack> items() {
        final Stream.Builder<ItemStack> b = Stream.builder();
        for (int i = 0; i < 9; i++) {
            final var resource = this.getResource(i);
            if (!resource.isEmpty()) {
                b.add(resource.toStack(1));
            }
        }

        return b.build();
    }

    @Override
    protected void onContentsChanged(int index, @NotNull ItemStack previousContents) {
        if(instance == null) return;

        if(!previousContents.isEmpty()) {
            final var isUpgrade = previousContents.has(CMDataComponents.UPGRADE_INSTANCE_ID);
            if(isUpgrade) {
                final var id = previousContents.get(CMDataComponents.UPGRADE_INSTANCE_ID);
                onUpgradeRemoved(id);
            }
        }

        var newContents = this.stacks.get(index);
        if(newContents.isEmpty()) return;

        if (!newContents.has(CMDataComponents.UPGRADE_INSTANCE_ID)) {
            var instance = UUID.randomUUID();
            newContents.set(CMDataComponents.UPGRADE_INSTANCE_ID, instance);
            onUpgradeApplied(newContents, instance);
            return;
        }

        final var id = newContents.get(CMDataComponents.UPGRADE_INSTANCE_ID);
        onUpgradeApplied(newContents, id);
    }

    private void onUpgradeApplied(@NotNull ItemStack itemStack, UUID newInstanceID) {
        if(instance == null) return;
        final var accessor = instance.getCapability(RoomCapabilities.UPGRADES);
        final var upgradeInstance = accessor.getOrCreateInstance(newInstanceID);

        final var upgradeList = itemStack.get(CMDataComponents.UPGRADE_LIST_COMPONENT);
        if (upgradeList != null)
            upgradeList.components()
                    .stream()
                    .flatMap(RoomUpgradeComponent::gatherEvents)
                    .filter(UpgradeAppliedEventListener.class::isInstance)
                    .map(UpgradeAppliedEventListener.class::cast)
                    .forEach(listener -> {
                        listener.handle(upgradeInstance);
                    });
    }

    private void onUpgradeRemoved(UUID instanceID) {
        if(instance == null) return;
        final var accessor = instance.getCapability(RoomCapabilities.UPGRADES);
        final var upgradeInstance = accessor.getOrCreateInstance(instanceID);

        upgradeInstance.upgradeComponents()
                .stream()
                .flatMap(RoomUpgradeComponent::gatherEvents)
                .filter(UpgradeRemovedEventListener.class::isInstance)
                .map(UpgradeRemovedEventListener.class::cast)
                .forEach(listener -> {
                    listener.handle(upgradeInstance);
                });
    }

    public void setRoomInstance(RoomInstance room) {
        this.instance = room;
    }
}
