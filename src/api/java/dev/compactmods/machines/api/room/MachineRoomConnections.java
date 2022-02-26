package dev.compactmods.machines.api.room;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;

public interface MachineRoomConnections {
    @NotNull Optional<ChunkPos> getConnectedRoom(int machineId);

    @NotNull Collection<Integer> getMachinesFor(ChunkPos chunkPos);

    void registerMachine(int machine);

    void registerRoom(ChunkPos roomChunk);

    void connectMachineToRoom(int machine, ChunkPos room);
}
