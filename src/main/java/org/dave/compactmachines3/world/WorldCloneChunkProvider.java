package org.dave.compactmachines3.world;

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.dave.compactmachines3.utility.ChunkUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WorldCloneChunkProvider implements IChunkProvider {
    World world;

    private final Chunk blankChunk;
    private final Long2ObjectMap<List<BlockPos>> toRender = new Long2ObjectOpenHashMap<>(8192);
    private final Long2ObjectMap<Chunk> loadedChunks = new Long2ObjectOpenHashMap<Chunk>(8192) {
        protected void rehash(int p_rehash_1_)
        {
            if (p_rehash_1_ > this.key.length)
            {
                super.rehash(p_rehash_1_);
            }
        }
    };

    public WorldCloneChunkProvider(World worldIn) {
        this.blankChunk = new EmptyChunk(worldIn, 0, 0);
        this.world = worldIn;
    }

    public Chunk loadChunkFromNBT(NBTTagCompound tag) {
        Chunk chunk = ChunkUtils.readChunkFromNBT(world, tag);
        chunk.markLoaded(true);
        this.loadedChunks.put(ChunkPos.asLong(chunk.x, chunk.z), chunk);

        List<BlockPos> toRender = new ArrayList<>();
        for(int x = 15; x >= 0; x--) {
            for(int y = 15; y >= 0; y--) {
                for(int z = 15; z >= 0; z--) {
                    BlockPos pos = new BlockPos(chunk.x * 16 + x, y + 40, z);
                    IBlockState state = chunk.getBlockState(pos);
                    if(state.getBlock() == Blocks.AIR) {
                        continue;
                    }

                    if(state.getBlock() == Blocks.BARRIER) {
                        continue;
                    }

                    toRender.add(pos);
                }
            }
        }

        this.toRender.put(ChunkPos.asLong(chunk.x, chunk.z), toRender);
        return chunk;
    }

    @Nullable
    @Override
    public Chunk getLoadedChunk(int x, int z) {
        return this.loadedChunks.get(ChunkPos.asLong(x, z));
    }

    @Override
    public Chunk provideChunk(int x, int z) {
        return MoreObjects.firstNonNull(this.getLoadedChunk(x, z), this.blankChunk);
    }

    public List<BlockPos> getRenderListForChunk(int x, int z) {
        return this.toRender.get(ChunkPos.asLong(x, z));
    }

    @Override
    public boolean tick() {
        return false;
    }

    @Override
    public String makeString() {
        return String.format("WorldCloneChunkCache: %d", this.loadedChunks.size());
    }

    @Override
    public boolean isChunkGeneratedAt(int x, int z) {
        return this.loadedChunks.containsKey(ChunkPos.asLong(x, z));
    }
}
