package dev.compactmods.machines.rooms.chunkloading;

import java.util.*;
import com.google.common.collect.ImmutableList;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.graph.CompactMachineConnectionGraph;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.MachineConnections;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.world.ForgeChunkManager;

public class CMRoomChunkloadingManager implements IRoomChunkloadingManager {

    private final MinecraftServer server;
    private final Map<ChunkPos, UUID> tickets;

    // TODO - Finish and serialize data
    // see ForcedChunksSaveData

    public CMRoomChunkloadingManager(MinecraftServer server) {
        this.server = server;
        this.tickets = new HashMap<>();
    }

    @Override
    public boolean roomIsLoaded(ChunkPos room) {
        return tickets.containsKey(room);
    }

    @Override
    public boolean hasAnyMachinesLoaded() {
        return !tickets.isEmpty();
    }

    @Override
    public void onMachineChunkUnload(int machine) {
        final Optional<ChunkPos> attachedRoom = getConnectedRoom(machine);
        attachedRoom.ifPresent(room -> {
            final Collection<Integer> machines = getConnectedMachines(room);
            switch (machines.size()) {
                case 0:
                case 1:
                    // No siblings or this is the only machine connected - unload the room
                    setChunkForced(room, false);
                    break;

                default:
                    // More than one machine attached to the room
                    // Need to see if any other machine is still loaded before decision
                    final CompactMachineData machData = CompactMachineData.get(server);
                    if (machData == null) {
                        // ohshi-
                        return;
                    }

                    // true if any connected machine is loaded; false otherwise
                    boolean anyRoomMachineLoaded = machines.stream()
                            .map(machData::getMachineLocation)
                            .anyMatch(machLocation -> machLocation.map(d -> d.isLoaded(server)).orElse(false));

                    if(!anyRoomMachineLoaded) {
                        setChunkForced(room, false);
                    }
                    break;
            }
        });
    }

    @Override
    public void onMachineChunkLoad(int machine) {
        final Optional<ChunkPos> attachedRoom = getConnectedRoom(machine);
        attachedRoom.ifPresent(room -> {
            // If there's already a ticket for the room, early exit - another machine has it loaded
            if (tickets.containsKey(room))
                return;

            setChunkForced(room, true);
        });
    }

    private Optional<CompactMachineConnectionGraph> getGraph() {
        final MachineConnections conns = MachineConnections.get(server);
        if (conns == null)
            return Optional.empty();

        return Optional.of(conns.graph);
    }

    private Optional<ChunkPos> getConnectedRoom(int machine) {
        return getGraph().flatMap(graph -> graph.getConnectedRoom(machine));
    }


    private Collection<Integer> getConnectedMachines(ChunkPos room) {
        return getGraph().map(graph -> graph.getMachinesFor(room))
                .map(connectedMachines -> {
                    switch (connectedMachines.size()) {
                        case 0:
                        case 1:
                            // release ticket
                            return Collections.<Integer>emptySet();

                        default:
                            // scan connected machines, if at least one is loaded then do not release
                            return ImmutableList.copyOf(connectedMachines);
                    }
                }).orElse(Collections.emptySet());
    }

    private Collection<Integer> getRoomConnectedMachines(int machine) {
        final CompactMachineConnectionGraph graph = getGraph().orElse(null);
        if (graph == null)
            return Collections.emptySet();

        return getConnectedRoom(machine)
                .map(this::getConnectedMachines)
                .orElse(Collections.emptySet());
    }

    private void setChunkForced(ChunkPos room, boolean force) {
        // If trying to force and room was not previously set up
        if (force && !tickets.containsKey(room)) {
            UUID newTicket = UUID.randomUUID();
            tickets.put(room, newTicket);
        }

        UUID ticket = tickets.get(room);

        ServerWorld compact = server.getLevel(Registration.COMPACT_DIMENSION);

        if (compact != null) {
            boolean alreadyForced = compact.getForcedChunks().stream()
                    .anyMatch(chunkLong -> chunkLong.equals(room.toLong()));

            // if force status requires change
            if(alreadyForced != force) {
                // prevent deadlock?
                compact.getChunk(room.x, room.z);

                boolean changed = ForgeChunkManager.forceChunk(compact, CompactMachines.MOD_ID,
                        ticket, room.x, room.z, force, true);

                if (!force && changed) {
                    tickets.remove(room);
                }
            }
        }

    }
}
