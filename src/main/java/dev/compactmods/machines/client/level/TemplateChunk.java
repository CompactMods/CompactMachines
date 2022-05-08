package dev.compactmods.machines.client.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.EmptyLevelChunk;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.FluidState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Taken from Immersive Engineering's manual code.
 * Source: https://github.com/BluSunrize/ImmersiveEngineering/blob/1.18.2/src/main/java/blusunrize/immersiveengineering/common/util/fakeworld/TemplateChunk.java
 */
public class TemplateChunk extends EmptyLevelChunk {
    private final Map<BlockPos, BlockState> blocksInChunk;
    private final Map<BlockPos, BlockEntity> tiles;
    private final Predicate<BlockPos> shouldShow;

    public TemplateChunk(Level worldIn, ChunkPos chunkPos, Map<BlockPos, StructureTemplate.StructureBlockInfo> blocksInChunk, Predicate<BlockPos> shouldShow) {
        super(worldIn, chunkPos, worldIn.getUncachedNoiseBiome(0, 0, 0));
        this.shouldShow = shouldShow;
        this.blocksInChunk = new HashMap<>();

        tiles = new HashMap<>();
        for (var pos : blocksInChunk.keySet()) {
            final var blockInfo = blocksInChunk.get(pos);
            this.blocksInChunk.put(pos, blockInfo.state);

            if(blockInfo.nbt != null) {
                BlockEntity tile = BlockEntity.loadStatic(blockInfo.pos, blockInfo.state, blockInfo.nbt);
                if (tile != null) {
                    tile.setLevel(worldIn);
                    tiles.put(blockInfo.pos, tile);
                }
            }
        }
    }

    @Nonnull
    @Override
    public BlockState getBlockState(@Nonnull BlockPos pos) {
        if (shouldShow.test(pos)) {
            var state = blocksInChunk.get(pos);
            if (state != null)
                return state;
        }

        return Blocks.VOID_AIR.defaultBlockState();
    }

    @Nonnull
    @Override
    public FluidState getFluidState(@Nonnull BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }


    @Nullable
    @Override
    public BlockEntity getBlockEntity(@Nonnull BlockPos pos, @Nonnull EntityCreationType creationMode) {
        if (!shouldShow.test(pos))
            return null;

        return tiles.get(pos);
    }
}