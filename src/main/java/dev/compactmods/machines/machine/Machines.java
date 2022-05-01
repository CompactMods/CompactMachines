package dev.compactmods.machines.machine;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.core.CMCommands;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.IRoomInformation;
import dev.compactmods.machines.api.room.MachineRoomConnections;
import dev.compactmods.machines.core.LevelBlockPosition;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.data.CompactMachineData;
import dev.compactmods.machines.machine.data.MachineToRoomConnections;
import dev.compactmods.machines.machine.exceptions.InvalidMachineStateException;
import dev.compactmods.machines.machine.exceptions.NonexistentMachineException;
import dev.compactmods.machines.room.RoomInformation;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.tunnel.data.RoomTunnelData;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Optional;

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
        final var machines = CompactMachineData.get(server);
        final var connections = MachineToRoomConnections.get(server);

        int nextId = machines.getNextMachineId();
        machines.setMachineLocation(nextId, new LevelBlockPosition(level.dimension(), machinePos));
        connections.registerMachine(nextId);
        return nextId;
    }

    public static boolean link(MinecraftServer server, int machine, ChunkPos room) {
        try {
            MachineRoomConnections connections = MachineToRoomConnections.get(server);
            connections.connectMachineToRoom(machine, room);
            return true;
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.error("Could not load world saved data while creating new machine and room.", e);
            return false;
        }
    }

    public static boolean destroy(MinecraftServer server, int machine) {
        MachineRoomConnections connections;
        try {
            connections = MachineToRoomConnections.get(server);
        } catch (MissingDimensionException e) {
            CompactMachines.LOGGER.error("Could not load world saved data while creating new machine and room.", e);
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

    public static void changeLink(MinecraftServer server, int machine, ChunkPos room) throws MissingDimensionException, NonexistentRoomException,
            NonexistentMachineException, InvalidMachineStateException {

        final var machineData = CompactMachineData.get(server);
        final var roomData = CompactRoomData.get(server);
        final var roomConnections = MachineToRoomConnections.get(server);

        final var currentRoomPos = roomConnections.getConnectedRoom(machine)
                .orElseThrow(() -> new NonexistentRoomException(room));

        final var machineInfo = machineData.getMachineLocation(machine)
                .orElseThrow(() -> new NonexistentMachineException(machine));

        final var machinePos = machineInfo.getBlockPosition();
        final var tunnelData = RoomTunnelData.get(server, currentRoomPos);
        final var tunnelGraph = tunnelData.getGraph();
        final var connectedTunnels = tunnelGraph.getConnections(machine).toList();

        if(!connectedTunnels.isEmpty()) {
            final var firstConnected = connectedTunnels.get(0);
            CompactMachines.LOGGER.error("Refusing to rebind machine to a different room: tunnel bound at {}", firstConnected);
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.REBIND_HAS_TUNNEL_CONNECTED, firstConnected));
        }

        if(!roomData.isRegistered(room)) {
            CompactMachines.LOGGER.error("Refusing to rebind machine to a different room: target room is not registered.");
            throw new CommandRuntimeException(TranslationUtil.command(CMCommands.CMD_ROOM_NOT_REGISTERED, room));
        }

        final var level = machineInfo.level(server);

        final var machineState = level.getBlockState(machinePos);
        if(!(machineState.getBlock() instanceof CompactMachineBlock machineBlock)) {
            CompactMachines.LOGGER.error("Refusing to rebind block at {}; not a machine block.", machinePos);
            throw new InvalidMachineStateException(machinePos, machineState, "Not a machine block.");
        }

        final var targetRoomData = roomData.getData(room);
        if(targetRoomData.getSize() != machineBlock.getSize()) {
            CompactMachines.LOGGER.error("Refusing to rebind block at {}; wrong size.", machinePos);
            throw new InvalidMachineStateException(machinePos, machineState, "Not the correct size.");
        }

        roomConnections.changeMachineLink(machine, room);
        if(level.getBlockEntity(machineInfo.getBlockPosition()) instanceof CompactMachineBlockEntity be) {
            be.updateMapping();
        }
    }

    public static LazyOptional<IDimensionalPosition> location(MinecraftServer server, int machine) throws MissingDimensionException {
        final var machineData = CompactMachineData.get(server);
        return machineData.getMachineLocation(machine);
    }

    public static Optional<IRoomInformation> getConnectedRoom(MinecraftServer server, int machine) throws MissingDimensionException, NonexistentRoomException {
        final var roomConnections = MachineToRoomConnections.get(server);
        final var compactLevel = server.getLevel(Registration.COMPACT_DIMENSION);

        final var connected = roomConnections.getConnectedRoom(machine);
        if(connected.isEmpty())
            return Optional.empty();

        final var cp = connected.get();
        final RoomSize size = Rooms.sizeOf(server, cp);

        return Optional.of(new RoomInformation(compactLevel, cp, size));
    }
}
