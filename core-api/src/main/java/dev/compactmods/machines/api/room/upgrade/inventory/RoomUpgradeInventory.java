package dev.compactmods.machines.api.room.upgrade.inventory;

import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.api.component.CMDataComponents;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.api.room.upgrade.RoomUpgradeComponent;
import dev.compactmods.machines.api.room.upgrade.event.lifecycle.UpgradeAppliedEventListener;
import dev.compactmods.machines.api.room.upgrade.event.lifecycle.UpgradeRemovedEventListener;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class RoomUpgradeInventory extends ItemStackHandler {

    private RoomInstance instance;
    private Map<Integer, UUID> upgradeIDs = new Int2ObjectOpenHashMap<>();

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
    protected void onLoad() {
        super.onLoad();
        this.scanSlotsForUpgrades();
    }

    private void scanSlotsForUpgrades() {
        for(int slot : upgradeIDs.keySet()) {
            var currentItem = getStackInSlot(slot);
            if(currentItem.isEmpty()) {
                upgradeIDs.remove(slot);
            } else {
                if(currentItem.has(CMDataComponents.UPGRADE_INSTANCE_ID))
                    upgradeIDs.put(slot, currentItem.get(CMDataComponents.UPGRADE_INSTANCE_ID));
            }
        }
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

    @Override
    protected void onContentsChanged(int slot) {
        if(instance == null) return;

        var item = getStackInSlot(slot);
        if (item.isEmpty() && upgradeIDs.containsKey(slot)) {
            final var oldID = upgradeIDs.remove(slot);
            onUpgradeRemoved(oldID);
            return;
        }

        if(item.isEmpty()) return;

        if (!item.has(CMDataComponents.UPGRADE_INSTANCE_ID)) {
            var instance = UUID.randomUUID();
            item.set(CMDataComponents.UPGRADE_INSTANCE_ID, instance);

            upgradeIDs.put(slot, instance);
            onUpgradeApplied(item, instance);
            return;
        }

        final var id = item.get(CMDataComponents.UPGRADE_INSTANCE_ID);
        onUpgradeApplied(item, id);
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
