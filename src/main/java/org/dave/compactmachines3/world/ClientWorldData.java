package org.dave.compactmachines3.world;

import net.minecraft.profiler.Profiler;
import net.minecraft.world.storage.WorldInfo;

public class ClientWorldData {
    public WorldInfo worldInfo;
    public WorldClone worldClone;

    public ClientWorldData init(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
        this.worldClone = new WorldClone(this.worldInfo, new WorldProviderMachines(), new Profiler(), true);

        return this;
    }

    public boolean isInitialized() {
        return worldInfo != null && worldClone != null;
    }


}
