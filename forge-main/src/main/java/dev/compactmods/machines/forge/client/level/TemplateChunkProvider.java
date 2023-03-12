package dev.compactmods.machines.forge.client.level;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Taken from Immersive Engineering's manual code.
 * Source: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18.2/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/TemplateChunkProvider.java
 */
public class TemplateChunkProvider extends ChunkSource {
    private final Map<ChunkPos, ChunkAccess> chunks;
    private final RenderingLevel world;
    private final LevelLightEngine lightManager;

    public TemplateChunkProvider(List<StructureTemplate.StructureBlockInfo> blocks, RenderingLevel world, Predicate<BlockPos> shouldShow) {
        this.world = world;
        this.lightManager = new LevelLightEngine(this, true, true);

        HashMap<BlockPos, StructureTemplate.StructureBlockInfo> blockInfo = new HashMap<>();
        blocks.forEach(sbi -> {
            blockInfo.put(sbi.pos, sbi);
        });

        chunks = loadChunkData(blockInfo, world, shouldShow);
    }

    @NotNull
    private Map<ChunkPos, ChunkAccess> loadChunkData(Map<BlockPos, StructureTemplate.StructureBlockInfo> blocks, RenderingLevel world, Predicate<BlockPos> shouldShow) {
        Map<ChunkPos, Map<BlockPos, StructureTemplate.StructureBlockInfo>> byChunk = new HashMap<>();
        for(var info : blocks.entrySet())
        {
            final var bc = byChunk.computeIfAbsent(new ChunkPos(info.getKey()), $ -> new HashMap<>());
            bc.put(info.getKey(), info.getValue());
        }

        return byChunk.entrySet().stream()
                .map(e -> Pair.of(e.getKey(), new TemplateChunk(world, e.getKey(), e.getValue(), shouldShow)))
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    public Stream<ChunkAccess> chunks() {
        return this.chunks.values().stream();
    }


    @Nullable
    @Override
    public ChunkAccess getChunk(int chunkX, int chunkZ, @NotNull ChunkStatus requiredStatus, boolean load)
    {
        return chunks.computeIfAbsent(new ChunkPos(chunkX, chunkZ), p -> {
            return new EmptyLevelChunk(world, p, world.getUncachedNoiseBiome(0, 0, 0));
        });
    }

    @Override
    public void tick(BooleanSupplier p_202162_, boolean p_202163_) {

    }

    @NotNull
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

    @NotNull
    @Override
    public LevelLightEngine getLightEngine()
    {
        return lightManager;
    }

    @NotNull
    @Override
    public BlockGetter getLevel()
    {
        return world;
    }
}
