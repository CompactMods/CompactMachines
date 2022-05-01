package dev.compactmods.machines.machine.data;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.room.MachineRoomConnections;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.graph.CompactMachineConnectionGraph;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class MachineToRoomConnections extends SavedData implements MachineRoomConnections {
    public static final String DATA_NAME = CompactMachines.MOD_ID + "_connections";

    private CompactMachineConnectionGraph graph;

    public MachineToRoomConnections() {
        graph = new CompactMachineConnectionGraph();
    }

    @Nonnull
    public static MachineRoomConnections get(MinecraftServer server) throws MissingDimensionException {
        ServerLevel compactWorld = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.error("No compact dimension found. Report this.");
            throw new MissingDimensionException();
        }

        DimensionDataStorage sd = compactWorld.getDataStorage();
        return sd.computeIfAbsent(MachineToRoomConnections::fromNbt, MachineToRoomConnections::new, DATA_NAME);
    }

    static MachineToRoomConnections fromNbt(CompoundTag nbt) {
        MachineToRoomConnections c = new MachineToRoomConnections();
        if (nbt.contains("graph")) {
            CompoundTag graphNbt = nbt.getCompound("graph");
            CompactMachineConnectionGraph.CODEC.parse(NbtOps.INSTANCE, graphNbt)
                    .resultOrPartial(CompactMachines.LOGGER::error)
                    .ifPresent(g -> c.graph = g);
        }

        return c;
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        CompactMachineConnectionGraph.CODEC
                .encodeStart(NbtOps.INSTANCE, graph)
                .resultOrPartial(CompactMachines.LOGGER::error)
                .ifPresent(gNbt -> nbt.put("graph", gNbt));

        return nbt;
    }

    /**
     * @deprecated Integer machines are planned to be removed in 1.19; plan is to replace them with dimensional positions
     * @param machineId
     * @return
     */
    @Override
    @Nonnull
    public Optional<ChunkPos> getConnectedRoom(int machineId) {
        return graph.getConnectedRoom(machineId);
    }

    /**
     * @param chunkPos Room to get machine IDs for
     * @return
     * @deprecated Integer machines are planned to be removed in 1.19; plan is to replace them with dimensional positions
     */
    @Override
    @Nonnull
    @Deprecated(since = "1.7.0")
    public Collection<Integer> getMachinesFor(ChunkPos chunkPos) {
        try {
            return graph.getMachinesFor(chunkPos);
        } catch (NonexistentRoomException e) {
            CompactMachines.LOGGER.error("Tried to get machine info for nonexistent room: " + chunkPos, e);
            return Collections.emptySet();
        }
    }

    /**
     * @deprecated Integer machines are planned to be removed in 1.19; plan is to replace them with dimensional positions
     * @param machine
     */
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
    public void unregisterRoom(ChunkPos roomChunk) {
        graph.removeRoom(roomChunk);
        setDirty();
    }

    /**
     * @deprecated Integer machines are planned to be removed in 1.19; plan is to replace them with dimensional positions
     * @param machine
     * @param room
     */
    @Override
    public void connectMachineToRoom(int machine, ChunkPos room) {
        graph.connectMachineToRoom(machine, room);
        setDirty();
    }

    @Override
    public void changeMachineLink(int machine, ChunkPos newRoom) {
        graph.disconnect(machine);
        graph.connectMachineToRoom(machine, newRoom);
        setDirty();
    }

    /**
     * @deprecated Integer machines are planned to be removed in 1.19; plan is to replace them with dimensional positions
     * @param machine
     */
    @Override
    public void disconnect(int machine) {
        graph.disconnectAndUnregister(machine);
        setDirty();
    }
}
