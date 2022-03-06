package dev.compactmods.machines.room.capability;

import dev.compactmods.machines.api.room.IRoomCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class RoomChunkCapabilities implements IRoomCapabilities {
    private final LevelChunk chunk;

    public RoomChunkCapabilities(LevelChunk chunk) {
        this.chunk = chunk;
    }

    @Override
    public <CapType> LazyOptional<CapType> getCapability(Capability<CapType> capability, Direction side) {
        return LazyOptional.empty();
    }
}
