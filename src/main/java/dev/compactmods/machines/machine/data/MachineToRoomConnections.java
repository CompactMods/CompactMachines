package dev.compactmods.machines.machine.data;

import java.util.Collection;
import java.util.Optional;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.MachineRoomConnections;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.graph.CompactMachineConnectionGraph;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

public class MachineToRoomConnections extends SavedData implements MachineRoomConnections {
    public static final String DATA_NAME = CompactMachines.MOD_ID + "_connections";

    private CompactMachineConnectionGraph graph;

    public MachineToRoomConnections() {
        graph = new CompactMachineConnectionGraph();
    }

    public static MachineRoomConnections get(MinecraftServer server) {
        ServerLevel compactWorld = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.error("No compact dimension found. Report this.");
            return null;
        }

        DimensionDataStorage sd = compactWorld.getDataStorage();
        return sd.computeIfAbsent(MachineToRoomConnections::fromNbt, MachineToRoomConnections::new, DATA_NAME);
    }

    public static MachineToRoomConnections fromNbt(CompoundTag nbt) {
        MachineToRoomConnections c = new MachineToRoomConnections();
        if (nbt.contains("graph")) {
            CompoundTag graphNbt = nbt.getCompound("graph");
            CompactMachineConnectionGraph.CODEC.parse(NbtOps.INSTANCE, graphNbt)
                    .resultOrPartial(CompactMachines.LOGGER::error)
                    .ifPresent(g -> c.graph = g);
        }

        return c;
    }

    @NotNull
    @Override
    public CompoundTag save(@NotNull CompoundTag nbt) {
        CompactMachineConnectionGraph.CODEC
                .encodeStart(NbtOps.INSTANCE, graph)
                .resultOrPartial(CompactMachines.LOGGER::error)
                .ifPresent(gNbt -> nbt.put("graph", gNbt));

        return nbt;
    }

    @Override
    @NotNull
    public Optional<ChunkPos> getConnectedRoom(int machineId) {
        return graph.getConnectedRoom(machineId);
    }

    @Override
    @NotNull
    public Collection<Integer> getMachinesFor(ChunkPos chunkPos) {
        return graph.getMachinesFor(chunkPos);
    }

    @Override
    public void registerMachine(int machine) {
        graph.addMachine(machine);
        setDirty();
    }

    @Override
    public void registerRoom(ChunkPos roomChunk) {
        graph.addRoom(roomChunk);
        setDirty();
    }

    @Override
    public void connectMachineToRoom(int machine, ChunkPos room) {
        graph.connectMachineToRoom(machine, room);
        setDirty();
    }
}
