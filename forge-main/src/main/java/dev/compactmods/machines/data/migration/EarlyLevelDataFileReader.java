package dev.compactmods.machines.data.migration;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Dynamic;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

public class EarlyLevelDataFileReader {

    private final LevelStorageSource.LevelDirectory directory;
    private static final ImmutableSet<ResourceKey<Level>> DEFAULT_LEVELS = ImmutableSet.of(Level.OVERWORLD, Level.NETHER, Level.END, CompactDimension.LEVEL_KEY);
    public EarlyLevelDataFileReader(LevelStorageSource.LevelDirectory directory) {
        this.directory = directory;
    }

    /**
     * Clones a file to do work upon, keeping the original in a safe state
     * @return
     */
    private Path makeSafeFile(String backupName) throws IOException {
        if(Files.exists(directory.dataFile())) {
            final var copied = directory.path().resolve(backupName);
            Files.copy(directory.dataFile(), copied, StandardCopyOption.REPLACE_EXISTING);
            return copied;
        }

        if(Files.exists(directory.oldDataFile())) {
            final var copied = directory.path().resolve(backupName);
            Files.copy(directory.oldDataFile(), copied, StandardCopyOption.REPLACE_EXISTING);
            return copied;
        }

        throw new IOException("No source file could be found.");
    }

    public Set<ResourceKey<Level>> dimensions() {
        CompactMachines.LOGGER.info("Starting dimension read from level files.");
        try {
            final var tempFile = makeSafeFile("cm5_early_dimensions.dat");
            final var dataFixer = DataFixers.getDataFixer();
            final var rootTag = NbtIo.readCompressed(tempFile.toFile()).getCompound("Data");

            int savedLevelVersion = rootTag.contains("DataVersion", 99) ? rootTag.getInt("DataVersion") : -1;
            Dynamic<Tag> dynamic = dataFixer.update(DataFixTypes.LEVEL.getType(), new Dynamic<>(NbtOps.INSTANCE, rootTag), savedLevelVersion,
                    SharedConstants.getCurrentVersion().getWorldVersion());

            final var updatedData = dataFixer.update(References.WORLD_GEN_SETTINGS, dynamic, savedLevelVersion, SharedConstants.getCurrentVersion().getWorldVersion());
            final var levels = WorldGenSettings.CODEC.parse(updatedData)
                    .resultOrPartial(Util.prefix("CM5-EarlyLevelReader: ", CompactMachines.LOGGER::error))
                    .map(WorldGenSettings::levels)
                    .orElse(DEFAULT_LEVELS);

            Files.deleteIfExists(tempFile);
            CompactMachines.LOGGER.info("Completed dimension read from level files.");
            return levels;
        } catch (IOException e) {
            CompactMachines.LOGGER.fatal("Failed to make a safe level file backup to read dimension info!");
            return DEFAULT_LEVELS;
        }
    }
}
