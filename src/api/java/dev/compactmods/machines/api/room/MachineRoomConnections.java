package dev.compactmods.machines.api.room;

import net.minecraft.world.level.ChunkPos;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

public interface MachineRoomConnections {
    @Nonnull
    Optional<ChunkPos> getConnectedRoom(int machineId);

    @Nonnull Collection<Integer> getMachinesFor(ChunkPos chunkPos);

    void registerMachine(int machine);

    void registerRoom(ChunkPos roomChunk);

    void connectMachineToRoom(int machine, ChunkPos room);

    void disconnect(int machine);

    void unregisterRoom(ChunkPos room);
}
