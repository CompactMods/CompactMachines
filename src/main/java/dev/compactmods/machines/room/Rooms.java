package dev.compactmods.machines.room;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.dimension.MissingDimensionException;
import dev.compactmods.machines.room.data.CompactRoomData;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.util.CompactStructureGenerator;
import dev.compactmods.machines.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.naming.OperationNotSupportedException;
import java.util.Optional;
import java.util.UUID;

public class Rooms {
    public static ChunkPos createNew(MinecraftServer serv, RoomSize size, UUID owner) throws MissingDimensionException {
        final var compactWorld = CompactDimension.forServer(serv);
        if (compactWorld == null)
            throw new MissingDimensionException();

        CompactRoomData rooms = CompactRoomData.get(compactWorld);

        int nextPosition = rooms.getNextSpiralPosition();
        Vec3i location = MathUtil.getRegionPositionByIndex(nextPosition);

        BlockPos newCenter = MathUtil.getCenterWithY(location, ServerConfig.MACHINE_FLOOR_Y.get());

        // Generate a new machine room
        CompactStructureGenerator.generateCompactStructure(compactWorld, size.toVec3(), newCenter);

        ChunkPos machineChunk = new ChunkPos(newCenter);
        try {
            rooms.createNew()
                    .owner(owner)
                    .size(size)
                    .chunk(machineChunk)
                    .register();
        } catch (OperationNotSupportedException e) {
            // room already registered somehow
            CompactMachines.LOGGER.warn(e);
        }

        return machineChunk;
    }

    public static RoomSize sizeOf(MinecraftServer server, ChunkPos room) throws NonexistentRoomException {
        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        return CompactRoomData.get(compactDim)
                .getData(room)
                .getSize();
    }

    public static IDimensionalPosition getSpawn(MinecraftServer server, ChunkPos room) {
        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        return CompactRoomData.get(compactDim).getSpawn(room);
    }

    public static boolean exists(MinecraftServer server, ChunkPos room) {
        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        return CompactRoomData.get(compactDim).isRegistered(room);
    }

    public static StructureTemplate getInternalBlocks(MinecraftServer server, ChunkPos room) throws MissingDimensionException, NonexistentRoomException {
        final var tem = new StructureTemplate();

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);

        final var bounds = roomInfo.getRoomBounds();
        final int inside = roomInfo.getSize().getInternalSize();
        tem.fillFromWorld(compactDim, new BlockPos(bounds.minX, bounds.minY - 1, bounds.minZ),
                new Vec3i(inside, inside + 1, inside), false, null);

        return tem;
    }

    public static void resetSpawn(MinecraftServer server, ChunkPos room) throws NonexistentRoomException {
        if (!exists(server, room))
            throw new NonexistentRoomException(room);

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);

        final var centerPoint = Vec3.atCenterOf(roomInfo.getCenter());
        final var newSpawn = centerPoint.subtract(0, (roomInfo.getSize().getInternalSize() / 2f), 0);

        data.setSpawn(room, newSpawn, Vec2.ZERO);
    }

    public static Optional<String> getRoomName(MinecraftServer server, ChunkPos room) throws NonexistentRoomException {
        if (!exists(server, room))
            throw new NonexistentRoomException(room);

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);
        return roomInfo.getName();
    }

    public static Optional<GameProfile> getOwner(MinecraftServer server, ChunkPos room) {
        if (!exists(server, room))
            return Optional.empty();

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        final var data = CompactRoomData.get(compactDim);

        try {
            final CompactRoomData.RoomData roomInfo = data.getData(room);
            final var ownerUUID = roomInfo.getOwner();

            return server.getProfileCache().get(ownerUUID);
        } catch (NonexistentRoomException e) {
            return Optional.empty();
        }
    }

    public static void updateName(MinecraftServer server, ChunkPos room, String newName) throws NonexistentRoomException {
        if (!exists(server, room))
            throw new NonexistentRoomException(room);

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);

        final var data = CompactRoomData.get(compactDim);
        final var roomInfo = data.getData(room);
        roomInfo.setName(newName);
        data.setDirty();
    }
}
