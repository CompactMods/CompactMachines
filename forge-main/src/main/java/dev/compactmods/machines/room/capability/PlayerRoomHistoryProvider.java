package dev.compactmods.machines.room.capability;

import dev.compactmods.machines.api.room.IRoomHistory;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerRoomHistoryProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    private final CMRoomHistory history;
    private final LazyOptional<IRoomHistory> opt;

    public PlayerRoomHistoryProvider() {
        this.history = new CMRoomHistory();
        this.opt = LazyOptional.of(() -> this.history);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == RoomCapabilities.ROOM_HISTORY)
            return opt.cast();

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("history", history.serializeNBT());
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if(nbt.contains("history")) {
            history.clear();
            history.deserializeNBT(nbt.getList("history", Tag.TAG_COMPOUND));
        }
    }
}
