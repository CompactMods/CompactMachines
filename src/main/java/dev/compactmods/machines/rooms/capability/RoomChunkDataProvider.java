package dev.compactmods.machines.rooms.capability;

import javax.annotation.Nullable;
import dev.compactmods.machines.core.Capabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;

public class RoomChunkDataProvider implements ICapabilitySerializable<CompoundTag> {
    private final LevelChunk chunk;
    private final RoomChunkData room;

    public RoomChunkDataProvider(LevelChunk chunk) {
        this.chunk = chunk;
        this.room = new RoomChunkData(chunk);
    }

    /**
     * Retrieves the Optional handler for the capability requested on the specific side.
     * The return value <strong>CAN</strong> be the same for multiple faces.
     * Modders are encouraged to cache this value, using the listener capabilities of the Optional to
     * be notified if the requested capability get lost.
     *
     * @param cap  The capability to check
     * @param side The Side to check from,
     *             <strong>CAN BE NULL</strong>. Null is defined to represent 'internal' or 'self'
     * @return The requested an optional holding the requested capability.
     */
    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == Capabilities.ROOM)
            return LazyOptional.of(() -> room).cast();

        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        // tag.put("room", room.serializeNBT());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        // room.deserializeNBT(nbt.getList("room", Tag.TAG_COMPOUND));
    }
}
