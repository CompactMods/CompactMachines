package org.dave.compactmachines3.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

public class WorldClone extends World {
    public WorldCloneChunkProvider providerClient;

    public WorldClone(WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(null, info, providerIn, profilerIn, client);
        this.providerClient = new WorldCloneChunkProvider(this);
        this.chunkProvider = this.providerClient;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return this.providerClient;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return this.providerClient.isChunkGeneratedAt(x, z);
    }
}
