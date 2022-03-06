package dev.compactmods.machines.tunnel.data;

import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.tunnel.graph.TunnelConnectionGraph;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;

public class RoomTunnelData extends SavedData {

    private final TunnelConnectionGraph graph;

    public RoomTunnelData() {
        graph = new TunnelConnectionGraph();
    }

    @Nonnull
    public static RoomTunnelData get(MinecraftServer server, ChunkPos room) throws MissingDimensionException {
        final var level = server.getLevel(Registration.COMPACT_DIMENSION);
        if (level == null) throw new MissingDimensionException();

        final var storage = level.getDataStorage();
        return storage.computeIfAbsent(RoomTunnelData::fromDisk, RoomTunnelData::new, "tunnels_" + room.x + "_" + room.z);
    }

    private static RoomTunnelData fromDisk(CompoundTag tag) {
        var instance = new RoomTunnelData();

        if(tag.contains("graph")) {
            var g = tag.getCompound("graph");
            instance.graph.deserializeNBT(g);
        }

        return instance;
    }



    @Nonnull
    @Override
    public CompoundTag save(CompoundTag tag) {
        var gData = graph.serializeNBT();
        tag.put("graph", gData);

        return tag;
    }

    @Nonnull
    public TunnelConnectionGraph getGraph() {
        return this.graph;
    }
}
