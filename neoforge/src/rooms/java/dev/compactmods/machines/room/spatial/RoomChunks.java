package dev.compactmods.machines.room.spatial;

import dev.compactmods.machines.api.room.spatial.IRoomChunks;
import net.minecraft.world.level.ChunkPos;

import java.util.Set;
import java.util.stream.Stream;

public record RoomChunks(Set<ChunkPos> chunks) implements IRoomChunks {
    @Override
    public Stream<ChunkPos> stream() {
        return chunks.stream();
    }

    @Override
    public boolean hasChunk(ChunkPos position) {
        return chunks.contains(position);
    }
}
