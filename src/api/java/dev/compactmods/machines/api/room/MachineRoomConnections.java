package dev.compactmods.machines.api.room;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

public interface MachineRoomConnections {
    @Nonnull
    Optional<ChunkPos> getConnectedRoom(ResourceKey<Level> machineLevel, BlockPos machinePos);

    @Nonnull Collection<Integer> getMachinesFor(ChunkPos chunkPos);

    void registerMachine(ResourceKey<Level> machineLevel, BlockPos machinePos);

    void registerRoom(ChunkPos roomChunk);

    void connectMachineToRoom(ResourceKey<Level> machineLevel, BlockPos machinePos, ChunkPos room);
    void changeMachineLink(ResourceKey<Level> machineLevel, BlockPos machinePos, ChunkPos newRoom);

    void disconnect(ResourceKey<Level> level, BlockPos pos);

    void unregisterRoom(ChunkPos room);
}
