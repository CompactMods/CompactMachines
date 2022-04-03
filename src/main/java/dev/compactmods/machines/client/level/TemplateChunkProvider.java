package dev.compactmods.machines.client.level;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.lighting.LevelLightEngine;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Taken from Immersive Engineering's manual code.
 * Source: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18.2/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/TemplateChunkProvider.java
 */
public class TemplateChunkProvider extends ChunkSource {
    private final Map<ChunkPos, ChunkAccess> chunks;
    private final Level world;
    private final LevelLightEngine lightManager;

    public TemplateChunkProvider(Map<BlockPos, BlockState> blocks, Level world, Predicate<BlockPos> shouldShow)
    {
        this.world = world;
        this.lightManager = new LevelLightEngine(this, true, true);
        Map<ChunkPos, Map<BlockPos, BlockState>> byChunk = new HashMap<>();
        for(var info : blocks.entrySet())
        {
            final var bc = byChunk.computeIfAbsent(new ChunkPos(info.getKey()), $ -> new HashMap<>());
            bc.put(info.getKey(), info.getValue());
        }

        chunks = byChunk.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), new TemplateChunk(world, e.getKey(), e.getValue(), shouldShow)))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, @Nonnull ChunkStatus requiredStatus, boolean load)
    {
        return chunks.computeIfAbsent(new ChunkPos(chunkX, chunkZ), p -> new EmptyLevelChunk(world, p, world.getUncachedNoiseBiome(0, 0, 0)));
    }

    @Override
    public void tick(BooleanSupplier p_202162_, boolean p_202163_) {

    }

    @Nonnull
    @Override
    public String gatherStats()
    {
        return "?";
    }

    @Override
    public int getLoadedChunksCount()
    {
        return 0;
    }

    @Nonnull
    @Override
    public LevelLightEngine getLightEngine()
    {
        return lightManager;
    }

    @Nonnull
    @Override
    public BlockGetter getLevel()
    {
        return world;
    }
}
