package dev.compactmods.machines.tunnel.definitions;

import dev.compactmods.machines.api.tunnels.ITunnel;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class ItemStorage implements ITunnel, INBTSerializable<CompoundTag> {

    private final LazyOptional<IItemHandler> laze;
    final ItemStackHandler handler;

    public ItemStorage(int buffer) {
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
