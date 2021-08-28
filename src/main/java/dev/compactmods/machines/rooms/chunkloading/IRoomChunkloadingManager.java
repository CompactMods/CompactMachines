package dev.compactmods.machines.rooms.chunkloading;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;

public interface IRoomChunkloadingManager {

    boolean roomIsLoaded(ChunkPos room);

    boolean hasAnyMachinesLoaded();

    void onMachineChunkUnload(int machine);

    void onMachineChunkLoad(int machine);
}
