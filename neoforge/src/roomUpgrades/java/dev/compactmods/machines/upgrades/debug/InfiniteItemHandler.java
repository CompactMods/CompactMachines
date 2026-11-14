package dev.compactmods.machines.upgrades.debug;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Objects;

/** A single never-ending slot of {@code item}: extracting it always succeeds, nothing can be inserted. */
public final class InfiniteItemHandler implements ResourceHandler<ItemResource> {

    private final ItemResource item;

    public InfiniteItemHandler(ItemResource item) {
        this.item = item;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return 0;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(index, size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        return resource.equals(item) ? amount : 0;
    }

    @Override
    public ItemResource getResource(int index) {
        Objects.checkIndex(index, size());
        return item;
    }

    @Override
    public long getAmountAsLong(int index) {
        Objects.checkIndex(index, size());
        return item.isEmpty() ? 0 : Integer.MAX_VALUE;
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        Objects.checkIndex(index, size());
        return resource.equals(item);
    }
}
