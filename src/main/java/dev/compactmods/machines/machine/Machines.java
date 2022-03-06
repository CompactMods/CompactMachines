package dev.compactmods.machines.machine;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.DimensionalPosition;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.machine.data.MachineToRoomConnections;
import dev.compactmods.machines.tunnel.data.RoomTunnelData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class Machines {
    public static boolean createAndLink(MinecraftServer server, Level level, BlockPos machinePos, CompactMachineBlockEntity tile, ChunkPos room) {
        try {
            int nextId = createNew(server, level, machinePos);
            tile.setMachineId(nextId);
            return link(server, nextId, room);

        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.fatal("Critical error while trying to create a new machine and link it to a new room.", e);
            return false;
        }
    }

    public static int createNew(MinecraftServer server, Level level, BlockPos machinePos) throws MissingDimensionException {
        CompactMachineData machines = CompactMachineData.get(server);

        var connections = MachineToRoomConnections.get(server);
        if (connections == null) {
            CompactMachines.LOGGER.error("Could not load world saved data while creating new machine and room.");
            throw new MissingDimensionException();
        }

        int nextId = machines.getNextMachineId();
        machines.setMachineLocation(nextId, new DimensionalPosition(level.dimension(), machinePos));
        connections.registerMachine(nextId);
        return nextId;
    }

    public static boolean link(MinecraftServer server, int machine, ChunkPos room) {
        var connections = MachineToRoomConnections.get(server);
        if (connections == null) {
            CompactMachines.LOGGER.error("Could not load world saved data while creating new machine and room.");
            return false;
        }

        connections.connectMachineToRoom(machine, room);
        return true;
    }

    public static boolean destroy(MinecraftServer server, int machine) {
        var connections = MachineToRoomConnections.get(server);
        if (connections == null) {
            CompactMachines.LOGGER.error("Could not load world saved data while creating new machine and room.");
            return false;
        }

        connections.getConnectedRoom(machine).ifPresent(room -> {
            try {
                var tunnels = RoomTunnelData.get(server, room);
                final var tunnelGraph = tunnels.getGraph();
                tunnelGraph.deleteMachine(machine);
                tunnels.setDirty();
            } catch (MissingDimensionException e) {
                e.printStackTrace();
            }

        });

        connections.disconnect(machine);
        return true;
    }
}
