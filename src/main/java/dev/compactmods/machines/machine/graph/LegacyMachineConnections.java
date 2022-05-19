package dev.compactmods.machines.machine.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LegacyMachineConnections extends SavedData {

    private static final String DATA_KEY = "compactmachines_connections";

    private final Map<Integer, ChunkPos> machineMapping;

    private LegacyMachineConnections() {
        this.machineMapping = new HashMap<>();
    }

    public static LegacyMachineConnections get(MinecraftServer server) throws MissingDimensionException {
        var compactDim = server.getLevel(Registration.COMPACT_DIMENSION);
        if(compactDim == null)
            throw new MissingDimensionException();

        return compactDim.getDataStorage().get(LegacyMachineConnections::load, DATA_KEY);
    }

    private static LegacyMachineConnections load(CompoundTag tag) {
        if(!tag.contains("graph"))
            return null;

        var graphTag = tag.getCompound("graph");
        if(!graphTag.contains("connections"))
            return null;

        LegacyMachineConnections tmp = new LegacyMachineConnections();
        final var connections = ConnectionInfoTag.CODEC.listOf()
                .fieldOf("connections")
                .codec()
                .parse(NbtOps.INSTANCE, graphTag)
                .getOrThrow(false, CompactMachines.LOGGER::error);

        // load all connections into result
        connections.forEach(conn -> conn.machines.forEach(mid -> tmp.machineMapping.putIfAbsent(mid, conn.room)));

        return tmp;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    public ChunkPos getConnectedRoom(int legacyMachineId) {
        if(!machineMapping.containsKey(legacyMachineId))
            return ChunkPos.ZERO;

        return machineMapping.get(legacyMachineId);
    }

    private record ConnectionInfoTag(ChunkPos room, List<Integer> machines) {
        public static final Codec<ConnectionInfoTag> CODEC = RecordCodecBuilder.create(i -> i.group(
                CodecExtensions.CHUNKPOS
                        .fieldOf("machine")
                        .forGetter(ConnectionInfoTag::room),

                Codec.INT.listOf()
                        .fieldOf("connections")
                        .forGetter(ConnectionInfoTag::machines)
        ).apply(i, ConnectionInfoTag::new));
    }
}
