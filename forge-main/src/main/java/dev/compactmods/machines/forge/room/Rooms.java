package dev.compactmods.machines.forge.room;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.forge.Registries;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Rooms {

    public static Supplier<IForgeRegistry<RoomTemplate>> TEMPLATES = Registries.ROOM_TEMPLATES
            .makeRegistry(() -> new RegistryBuilder<RoomTemplate>()
                    .dataPackRegistry(RoomTemplate.CODEC, RoomTemplate.CODEC));

    public static void prepare() {
    }

    /*
 TODO - Revisit with furnace recipe
    public static boolean destroy(MinecraftServer server, ChunkPos room) throws MissingDimensionException, NonexistentRoomException {
        final var compactDim = server.getLevel(Registration.COMPACT_DIMENSION);
        if (compactDim == null)
            throw new MissingDimensionException();

        var roomData = CompactRoomData.get(compactDim);
        if (!roomData.isRegistered(room)) {
            throw new NonexistentRoomException(room);
        }

        final var roomBounds = roomData.getBounds(room);
        final var innerBounds = roomBounds.deflate(1);

        final var states = compactDim.getBlockStates(innerBounds)
                .collect(Collectors.toSet());

        final var nonAir = states.stream()
                .filter(state -> !state.isAir())
                .findAny();

        if (nonAir.isPresent()) {
            CompactMachines.LOGGER.error("Refusing to delete room at {}; non-air blocks exist inside the room. First match: {}", room, nonAir.get());
            return false;
        }

        // clear tunnel connection info
        final var tunnels = RoomTunnelData.getFile(server, room);
        final var filename = RoomTunnelData.getDataFilename(room);
        if (!tunnels.delete()) {
            CompactMachines.LOGGER.warn("Could not delete tunnel data for room {}; clearing the connection graph as an alternative.", room);
            CompactMachines.LOGGER.warn("Data file to delete: {}", filename);

            var td = RoomTunnelData.forRoom(server, room);
            td.getGraph().clear();
            td.setDirty();
        } else {
            // File deletion successful, delete cached data
            final var compactDataCache = compactDim.getDataStorage().cache;
            compactDataCache.remove(filename);
        }

        // reset everything for the room boundary
        BlockPos.betweenClosedStream(roomBounds.inflate(1))
                .forEach(p -> compactDim.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));

        // Remove room registration
        roomData.remove(room);

        // Disconnect all machines
        var conns = MachineToRoomConnections.forDimension(server);
        var d = CompactMachineData.get(server);

        var connected = conns.getMachinesFor(room);
        for (int mid : connected) {
            var location = d.getMachineLocation(mid);
            location.ifPresent(p -> {
                var pos = p.getBlockPosition();
                var l = p.level(server);
                if (l.getBlockEntity(pos) instanceof TunnelWallEntity tunn) {
                    tunn.disconnect();
                }
            });
        }

        conns.unregisterRoom(room);
        return true;
    }
*/

    public static CompletableFuture<StructureTemplate> getInternalBlocks(MinecraftServer server, String room) throws MissingDimensionException, NonexistentRoomException {
        final var tem = new StructureTemplate();

        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
        final var provider = CompactRoomProvider.instance(compactDim);

        final var chunkSource = compactDim.getChunkSource();
        return provider.forRoom(room).map(reg -> {
            final var chunkLoading = reg.chunks()
                    .map(cp -> chunkSource.getChunkFuture(cp.x, cp.z, ChunkStatus.FULL, true))
                    .toList();

            final var awaitAllChunks = CompletableFuture.allOf(chunkLoading.toArray(new CompletableFuture[chunkLoading.size()]));

            return awaitAllChunks.thenApply(ignored -> {
                final var bounds = reg.outerBounds();
                tem.fillFromWorld(compactDim,
                        new BlockPos(bounds.minX, bounds.minY - 1, bounds.minZ),
                        new Vec3i(bounds.getXsize(), bounds.getYsize() + 1, bounds.getZsize()),
                        false, Blocks.AIR
                );

                return tem;
            });
        }).orElse(CompletableFuture.completedFuture(tem));
    }

    public static Optional<String> getRoomName(MinecraftServer server, String room) throws NonexistentRoomException {
//        if (!exists(server, room))
//            throw new NonexistentRoomException(room);
//
//        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
//
//        final var data = RoomData.get(compactDim);
//        final var roomInfo = data.getData(room);
//        return roomInfo.getName();
        return Optional.empty();
    }

    public static void updateName(MinecraftServer server, String room, String newName) throws NonexistentRoomException {
//        if (!exists(server, room))
//            throw new NonexistentRoomException(room);
//
//        final var compactDim = server.getLevel(CompactDimension.LEVEL_KEY);
//
//        final var data = RoomData.get(compactDim);
//        final var roomInfo = data.getData(room);
//        roomInfo.setName(newName);
//        data.setDirty();
    }
}
