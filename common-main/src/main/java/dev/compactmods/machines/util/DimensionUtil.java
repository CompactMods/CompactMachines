package dev.compactmods.machines.util;

import com.mojang.serialization.JsonOps;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.LoggingUtil;
import net.minecraft.SharedConstants;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DimensionUtil {

    private static final Logger LOG = LoggingUtil.modLog();

    @SuppressWarnings("deprecation") // because we call the forge internal method server#markWorldsDirty
    public static void createAndRegisterWorldAndDimension(final MinecraftServer server) {
        // get everything we need to create the dimension and the dimension
        final ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        // dimension keys have a 1:1 relationship with dimension keys, they have the same IDs as well
        final ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, CompactDimension.LEVEL_KEY.location());

        final var serverResources = server.getResourceManager();

        // only back up dimension.dat in production
        if (!doLevelFileBackup(server)) return;

        var reg = server.registryAccess();
        var cmDimType = reg.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
                .get(CompactDimension.DIM_TYPE_KEY);

        var ops = RegistryOps.create(JsonOps.INSTANCE, reg);

        var resourceAccess = RegistryResourceAccess.forResourceManager(serverResources);
        var dims = resourceAccess.listResources(Registry.DIMENSION_REGISTRY);

        // TODO - Revisit
//        resourceAccess.getResource(Registration.COMPACT_DIMENSION).ifPresent(lev -> {
//            var parsed = lev.parseElement(JsonOps.INSTANCE, LevelStem.CODEC);
//
//            var stem = parsed.result().orElseThrow().value();
//
//            // the int in create() here is radius of chunks to watch, 11 is what the server uses when it initializes worlds
//            final ChunkProgressListener chunkProgressListener = server.progressListenerFactory.create(11);
//            final Executor executor = server.executor;
//            final LevelStorageSource.LevelStorageAccess anvilConverter = server.storageSource;
//            final WorldData worldData = server.getWorldData();
//            final WorldGenSettings worldGenSettings = worldData.worldGenSettings();
//            final DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());
//
//            // now we have everything we need to create the dimension and the dimension
//            // this is the same order server init creates levels:
//            // the dimensions are already registered when levels are created, we'll do that first
//            // then instantiate dimension, add border listener, add to map, fire world load event
//
//            // register the actual dimension
//            if (worldGenSettings.dimensions() instanceof MappedRegistry<LevelStem> stems) {
//                stems.unfreeze();
//                Registry.register(stems, dimensionKey, stem);
//                stems.freeze();
//            } else {
//                CompactMachines.LOGGER.fatal("Failed to re-register compact machines dimension; registry was not the expected class type.");
//                return;
//            }
//
//            // create the world instance
//            final ServerLevel newWorld = new ServerLevel(
//                    server,
//                    executor,
//                    anvilConverter,
//                    derivedLevelData,
//                    Registration.COMPACT_DIMENSION,
//                    Holder.direct(cmDimType),
//                    chunkProgressListener,
//                    stem.generator(),
//                    worldGenSettings.isDebug(),
//                    net.minecraft.world.level.biome.BiomeManager.obfuscateSeed(worldGenSettings.seed()),
//                    ImmutableList.of(), // "special spawn list"
//                    false // "tick time", true for overworld, always false for nether, end, and json dimensions
//            );
//
//            /*
//             add world border listener, for parity with json dimensions
//             the vanilla behaviour is that world borders exist in every dimension simultaneously with the same size and position
//             these border listeners are automatically added to the overworld as worlds are loaded, so we should do that here too
//             TODO if world-specific world borders are ever added, change it here too
//            */
//            overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newWorld.getWorldBorder()));
//
//            // register dimension
//            map.put(Registration.COMPACT_DIMENSION, newWorld);
//
//            // update forge's world cache so the new dimension can be ticked
//            server.markWorldsDirty();
//
//            // fire world load event
//            MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(newWorld));
//        });
    }

    public static boolean doLevelFileBackup(MinecraftServer server) {
        var levelRoot = server.getWorldPath(LevelResource.ROOT);
        var levelFile = server.getWorldPath(LevelResource.LEVEL_DATA_FILE);

        var formatter = DateTimeFormatter.ofPattern("'cm4-dimension-'yyyyMMdd-HHmmss'.dat'");
        var timestamp = formatter.format(ZonedDateTime.now());
        try {
            Files.copy(levelFile, levelRoot.resolve(timestamp));
        } catch (IOException e) {
            LOG.error("Failed to backup dimension.dat file before modification; canceling register dim attempt.");
            return false;
        }

        return true;
    }

    @NotNull
    public static Path getDataFolder(@NotNull Path rootDir, ResourceKey<Level> key) {
        final var dimPath = DimensionType.getStorageFolder(key, rootDir);
        return dimPath.resolve("data");
    }

    @NotNull
    public static Path getDataFolder(@NotNull LevelStorageSource.LevelDirectory levelDir, ResourceKey<Level> key) {
        final var dimPath = DimensionType.getStorageFolder(key, levelDir.path());
        return dimPath.resolve("data");
    }

    @NotNull
    public static DimensionDataStorage getDataStorage(@NotNull LevelStorageSource.LevelDirectory levelDir, ResourceKey<Level> key) {
        final var folder = getDataFolder(levelDir, key).toFile();
        final var fixer = DataFixers.getDataFixer();
        return new DimensionDataStorage(folder, fixer);
    }

    public static CompoundTag readSavedFile(@NotNull DimensionDataStorage storage, String dataKey) throws IOException {
        final var currVersion = SharedConstants.getCurrentVersion().getWorldVersion();
        final var nbt = storage.readTagFromDisk(dataKey, currVersion);
        return nbt.getCompound("data");
    }
}
