package dev.compactmods.machines.forge;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.forge.data.migration.EarlyLevelDataFileReader;
import dev.compactmods.machines.forge.data.migration.Pre520RoomDataMigrator;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.event.ModMismatchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataUpgrader {

    private static final Logger UPDATER_LOGGER = LogManager.getLogger(Constants.MOD_ID);
    private static final Marker UPDATER = MarkerManager.getMarker("pre520_data_updater");

    static final DateTimeFormatter DATETIME_FORMAT = new DateTimeFormatterBuilder()
            .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
            .appendLiteral('-')
            .appendValue(ChronoField.MONTH_OF_YEAR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.DAY_OF_MONTH, 2)
            .appendLiteral('_')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral('-')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter();

    @SubscribeEvent
    public static void onUpgrade(final ModMismatchEvent mismatch) {
        mismatch.getVersionDifference(Constants.MOD_ID).ifPresent(versions -> {
            // If we're migrating from a version pre-5.2.0 (1.19.2)
            if (versions.oldVersion().compareTo(new DefaultArtifactVersion("5.2.0")) < 0) {
                // do upgrade
                try {
                    ModDataUpgrader.doUpgrade(mismatch.getLevelDirectory());
                    mismatch.markResolved(Constants.MOD_ID);
                } catch (IOException e) {
                    CompactMachines.LOGGER.fatal("Exception occurred while trying to upgrade world data.", e);
                }
            }
        });
    }

    public static void doUpgrade(@Nullable LevelStorageSource.LevelDirectory levelDirectory) throws IOException {
        if (levelDirectory != null) {
            final var dataStore = CompactDimension.getDataStorage(levelDirectory);
            final var backupDir = levelDirectory.path().resolve("cm5_backup");

            final var earlyFileReader = new EarlyLevelDataFileReader(levelDirectory);

            Files.deleteIfExists(backupDir);
            Files.createDirectories(backupDir);

            var oldData = Pre520RoomDataMigrator.loadOldRoomData(dataStore);

            if (oldData.oldRoomData().isEmpty()) {
                UPDATER_LOGGER.info(UPDATER, "No room data found to update. Exiting early.");
                return;
            }

            Pre520RoomDataMigrator.makeRoomDataBackup(dataStore, backupDir);
            if(!dataStore.getDataFile(Pre520RoomDataMigrator.ROOM_DATA_NAME).delete())
                UPDATER_LOGGER.warn(UPDATER, "Failed to remove original room data file after backup; things might get weird.");

            final var roomChunkLookup = oldData.roomChunkLookup();

            Pre520RoomDataMigrator.addMissingRoomEntries(dataStore, CompactRoomProvider.empty(), oldData);

            UPDATER_LOGGER.debug(UPDATER, "Updating room data for {} rooms.", roomChunkLookup.size());

            final var levels = earlyFileReader.dimensions();
            Pre520RoomDataMigrator.migrateConnectionInfo(levelDirectory, roomChunkLookup, levels, backupDir);

            Pre520RoomDataMigrator.migrateTunnelFiles(levelDirectory, roomChunkLookup, backupDir);

            makeBackupZip(levelDirectory.path().resolve("cm520-data-backup-%s.zip".formatted(LocalDateTime.now().format(DATETIME_FORMAT))), backupDir);
            PathUtils.delete(backupDir);
        }
    }


    private static void makeBackupZip(Path destinationFile, Path backupDir) throws IOException {
        final ZipOutputStream zipoutputstream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(destinationFile)));

        try {
            Files.walkFileTree(backupDir, new SimpleFileVisitor<>() {
                public FileVisitResult visitFile(Path filename, BasicFileAttributes attrs) throws IOException {
                    String shortFilename = backupDir.relativize(filename).toString().replace('\\', '/');
                    zipoutputstream.putNextEntry(new ZipEntry(shortFilename));

                    com.google.common.io.Files.asByteSource(filename.toFile()).copyTo(zipoutputstream);
                    zipoutputstream.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (Throwable throwable1) {
            try {
                zipoutputstream.close();
            } catch (Throwable throwable) {
                throwable1.addSuppressed(throwable);
            }

            throw throwable1;
        }

        zipoutputstream.close();
    }
}