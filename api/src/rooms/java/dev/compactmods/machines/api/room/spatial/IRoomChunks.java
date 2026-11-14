package dev.compactmods.machines.api.room.spatial;

import net.minecraft.world.level.ChunkPos;

import java.util.stream.Stream;

public interface IRoomChunks {

    Stream<ChunkPos> stream();

    boolean hasChunk(ChunkPos position);
}
