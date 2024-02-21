package dev.compactmods.machines.neoforge.room;

import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.util.concurrent.CompletableFuture;

public class RoomBlocks {

    public static CompletableFuture<StructureTemplate> getInternalBlocks(MinecraftServer server, String room) throws MissingDimensionException {
        final var tem = new StructureTemplate();

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        final var chunkSource = compactDim.getChunkSource();
        return RoomApi.room(room).map(instance -> {
            final var chunkLoading = RoomApi.chunks(room)
                    .stream()
                    .map(cp -> chunkSource.getChunkFuture(cp.x, cp.z, ChunkStatus.FULL, true))
                    .toList();

            final var awaitAllChunks = CompletableFuture.allOf(chunkLoading.toArray(new CompletableFuture[chunkLoading.size()]));

            return awaitAllChunks.thenApply(ignored -> {
                final var bounds = instance.boundaries().outerBounds();
                tem.fillFromWorld(compactDim,
                        BlockPos.containing(bounds.minX, bounds.minY - 1, bounds.minZ),
                        new Vec3i((int) bounds.getXsize(), (int) bounds.getYsize() + 1, (int) bounds.getZsize()),
                        false, Blocks.AIR
                );

                return tem;
            });
        }).orElse(CompletableFuture.completedFuture(tem));
    }
}
