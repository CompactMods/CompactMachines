package dev.compactmods.machines.tunnel.definitions;

import dev.compactmods.machines.api.tunnels.ITunnel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

public class ItemStorage implements ITunnel, INBTSerializable<CompoundTag> {

    Direction side;
    private final LazyOptional<IItemHandler> laze;
    final ItemStackHandler handler;

    public ItemStorage(int buffer, Direction side) {
        this.handler = new ItemStackHandler(buffer);
        this.side = side;
        this.laze = LazyOptional.of(this::getItems);
    }

    private @NotNull IItemHandler getItems() {
        return handler;
    }

    public <CapType> LazyOptional<CapType> lazy() {
        return laze.cast();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("side", side.getSerializedName());
        tag.put("items", handler.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.side = Direction.byName(nbt.getString("side"));
        handler.deserializeNBT(nbt.getCompound("items"));
    }
}
