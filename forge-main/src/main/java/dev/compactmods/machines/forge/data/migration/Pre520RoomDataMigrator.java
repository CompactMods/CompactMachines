package dev.compactmods.machines.forge.data.migration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.forge.machine.block.LegacySizedCompactMachineBlock;
import dev.compactmods.machines.forge.tunnel.graph.TunnelConnectionGraph;
import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.RoomSize;
import dev.compactmods.machines.codec.CodecExtensions;
import dev.compactmods.machines.machine.graph.DimensionMachineGraph;
import dev.compactmods.machines.room.RoomCodeGenerator;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.util.DimensionUtil;
import dev.compactmods.machines.util.SavedDataHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"removal", "deprecation"})
public class Pre520RoomDataMigrator {

    public static final String ROOM_DATA_NAME = Constants.MOD_ID + "_rooms";
    private static final Logger UPDATER_LOGGER = LogManager.getLogger(Constants.MOD_ID);
    private static final Marker UPDATER = MarkerManager.getMarker("room_data_updater");

    public record RoomDataPre520(UUID owner, BlockPos center, Vec3 spawn, RoomSize size, Optional<String> name) {

        public static final Codec<RoomDataPre520> CODEC = RecordCodecBuilder.create(i -> i.group(
                CodecExtensions.UUID_STRING.fieldOf("owner").forGetter(RoomDataPre520::owner),
                BlockPos.CODEC.fieldOf("center").forGetter(RoomDataPre520::center),
                CodecExtensions.VECTOR3D.fieldOf("spawn").forGetter(RoomDataPre520::spawn),
                RoomSize.CODEC.fieldOf("size").forGetter(RoomDataPre520::size),
                Codec.STRING.optionalFieldOf("name").forGetter(RoomDataPre520::name)
        ).apply(i, RoomDataPre520::new));
    }

    public record RoomDataLoadResult(HashMap<String, RoomDataPre520> oldRoomData, HashMap<ChunkPos, String> roomChunkLookup) {
    }

    public static String getOldTunnelFilename(ChunkPos oldRoom) {
        return "tunnels_%s_%s".formatted(oldRoom.x, oldRoom.z);
    }

    public static RoomDataLoadResult loadOldRoomData(DimensionDataStorage dataStore) throws IOException {
        AtomicReference<RoomDataLoadResult> result = new AtomicReference<>();
        SavedDataHelper.processFile(dataStore, ROOM_DATA_NAME, nbt -> result.set(loadOldRoomData(nbt)));
        return result.get();
    }

    public static RoomDataLoadResult loadOldRoomData(CompoundTag nbt) {
        final HashMap<String, RoomDataPre520> oldRoomData = new HashMap<>();
        final HashMap<ChunkPos, String> roomChunkLookup = new HashMap<>();

        final var data = RoomDataPre520.CODEC.listOf()
                .parse(NbtOps.INSTANCE, nbt.getList("machines", Tag.TAG_COMPOUND))
                .resultOrPartial(UPDATER_LOGGER::error)
                .orElse(Collections.emptyList());

        data.forEach(d -> {
            ChunkPos chunk = new ChunkPos(d.center());
            String newId = RoomCodeGenerator.generateRoomId();
            roomChunkLookup.put(chunk, newId);
            oldRoomData.put(newId, d);
        });

        return new RoomDataLoadResult(oldRoomData, roomChunkLookup);
    }

    public static void addMissingRoomEntries(DimensionDataStorage dataStore, CompactRoomProvider provider, RoomDataLoadResult oldData) {
        oldData.oldRoomData().forEach((code, roomInfo) -> {
            final var roomChunk = new ChunkPos(roomInfo.center);
            if (!provider.isRoomChunk(roomChunk)) {
                // Room hasn't been registered yet, map to migrated code
                var legTemplate = LegacySizedCompactMachineBlock.getLegacyTemplate(roomInfo.size);
                provider.registerNew(code, builder -> builder
                        .setColor(legTemplate.color())
                        .setDimensions(roomInfo.size.toVec3())
                        .setCenter(roomInfo.center)
                        .setSpawn(roomInfo.spawn, Vec2.ZERO)
                        .setOwner(roomInfo.owner));
            }
        });

        SavedDataHelper.saveFile(dataStore, CompactRoomProvider.DATA_NAME, provider);
    }

    public static void migrateConnectionInfo(LevelStorageSource.LevelDirectory levelDirectory, HashMap<ChunkPos, String> roomChunkLookup,
                                             Set<ResourceKey<Level>> levels, Path backupDir) throws IOException {

        for (var level : levels) {
            final var dimDataStore = DimensionUtil.getDataStorage(levelDirectory, level);
            final var connFile = dimDataStore.getDataFile(DimensionMachineGraph.DATA_KEY);
            if (connFile.exists()) {
                final var thisDimDir = DimensionUtil.getDataFolder(backupDir, level);
                Files.createDirectories(thisDimDir);

                UPDATER_LOGGER.debug(UPDATER, "Updating connection info for dimension: {}", level.location());
                Files.copy(connFile.toPath(),  thisDimDir.resolve(connFile.getName() + ".backup"));

                final var machineGraphNbt = DimensionUtil.readSavedFile(dimDataStore, DimensionMachineGraph.DATA_KEY);
                machineGraphNbt.getCompound("graph")
                        .getList("connections", Tag.TAG_COMPOUND)
                        .forEach(machConnTag -> {
                            // Loop machine-room connection info, replace room pos with room code
                            if (machConnTag instanceof CompoundTag ct) {
                                final var oldChunk = ct.getIntArray("room");
                                final var oldRoomPos = new ChunkPos(oldChunk[0], oldChunk[1]);
                                if (roomChunkLookup.containsKey(oldRoomPos)) {
                                    final var newCode = roomChunkLookup.get(oldRoomPos);
                                    ct.remove("room");
                                    ct.putString("room", newCode);
                                    UPDATER_LOGGER.debug(UPDATER, "Assigning new code to room {}; code: {}", oldRoomPos.toString(), newCode);
                                }
                            }
                        });

                final var dimPath = DimensionType.getStorageFolder(level, levelDirectory.path());
                NbtIo.writeCompressed(machineGraphNbt, dimPath.resolve("data").resolve(DimensionMachineGraph.DATA_KEY + ".dat").toFile());
            }
        }
    }

    public static void migrateTunnelFiles(LevelStorageSource.LevelDirectory levelDirectory, HashMap<ChunkPos, String> roomChunkLookup, Path backupDir)
            throws IOException {
        Files.createDirectories(backupDir.resolve("tunnels"));

        final var dataStore = CompactDimension.getDataStorage(levelDirectory);
        for(final var room : roomChunkLookup.entrySet()) {
            final var prevFilename = getOldTunnelFilename(room.getKey());
            final var oldFile = dataStore.getDataFile(prevFilename);
            final var newFile = dataStore.getDataFile(TunnelConnectionGraph.getDataFilename(room.getValue()));
            if(oldFile.exists()) {
                final var oldFilePath = oldFile.toPath();
                Files.copy(oldFilePath, backupDir.resolve("tunnels").resolve(prevFilename + ".dat.backup"));
                Files.copy(oldFilePath, newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.delete(oldFilePath);
            }
        }
    }

    public static void makeRoomDataBackup(DimensionDataStorage dataStore, Path backupDir) throws IOException {
        final var moveTo = DimensionUtil.getDataFolder(backupDir, CompactDimension.LEVEL_KEY);
        Files.createDirectories(moveTo);
        Files.copy(dataStore.getDataFile(ROOM_DATA_NAME).toPath(), moveTo.resolve(ROOM_DATA_NAME.concat(".dat")));
    }
}
