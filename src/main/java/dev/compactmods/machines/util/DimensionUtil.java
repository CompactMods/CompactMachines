package dev.compactmods.machines.util;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executor;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.core.Registration;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryResourceAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

public class DimensionUtil {

    @SuppressWarnings("deprecation") // because we call the forge internal method server#markWorldsDirty
    public static void createAndRegisterWorldAndDimension(final MinecraftServer server) {
        final var map = server.forgeGetWorldMap();

        // get everything we need to create the dimension and the level
        final ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        // dimension keys have a 1:1 relationship with level keys, they have the same IDs as well
        final ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, Registration.COMPACT_DIMENSION.location());

        final var serverResources = server.getResourceManager();

        // only back up level.dat in production
        if (FMLEnvironment.production && !doLevelFileBackup(server)) return;

        var reg = server.registryAccess();
        var cmDimType = reg.registryOrThrow(Registry.DIMENSION_TYPE_REGISTRY)
                .get(Registration.COMPACT_DIMENSION_DIM_TYPE);

        var ops = RegistryReadOps.create(JsonOps.INSTANCE, serverResources, reg);

        var resourceAccess = RegistryResourceAccess.forResourceManager(serverResources);
        var dims = resourceAccess.listResources(Registry.DIMENSION_REGISTRY);

        var cmDim = dims.stream()
                .filter(d -> d.location().equals(Registration.COMPACT_DIMENSION.location()))
                .findFirst();

        cmDim.ifPresent(lev -> {
            var parsed = resourceAccess.parseElement(ops, Registry.LEVEL_STEM_REGISTRY, dimensionKey, LevelStem.CODEC);

            var stem = parsed.orElseThrow()
                    .result().orElseThrow()
                    .value();

            // the int in create() here is radius of chunks to watch, 11 is what the server uses when it initializes worlds
            final ChunkProgressListener chunkProgressListener = server.progressListenerFactory.create(11);
            final Executor executor = server.executor;
            final LevelStorageSource.LevelStorageAccess anvilConverter = server.storageSource;
            final WorldData worldData = server.getWorldData();
            final WorldGenSettings worldGenSettings = worldData.worldGenSettings();
            final DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());

            // now we have everything we need to create the dimension and the level
            // this is the same order server init creates levels:
            // the dimensions are already registered when levels are created, we'll do that first
            // then instantiate level, add border listener, add to map, fire world load event

            // register the actual dimension
            worldGenSettings.dimensions().register(dimensionKey, stem, Lifecycle.experimental());

            // create the world instance
            final ServerLevel newWorld = new ServerLevel(
                    server,
                    executor,
                    anvilConverter,
                    derivedLevelData,
                    Registration.COMPACT_DIMENSION,
                    cmDimType,
                    chunkProgressListener,
                    stem.generator(),
                    worldGenSettings.isDebug(),
                    net.minecraft.world.level.biome.BiomeManager.obfuscateSeed(worldGenSettings.seed()),
                    ImmutableList.of(), // "special spawn list"
                    false // "tick time", true for overworld, always false for nether, end, and json dimensions
            );

            /*
             add world border listener, for parity with json dimensions
             the vanilla behaviour is that world borders exist in every dimension simultaneously with the same size and position
             these border listeners are automatically added to the overworld as worlds are loaded, so we should do that here too
             TODO if world-specific world borders are ever added, change it here too
            */
            overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newWorld.getWorldBorder()));

            // register level
            map.put(Registration.COMPACT_DIMENSION, newWorld);

            // update forge's world cache so the new level can be ticked
            server.markWorldsDirty();

            // fire world load event
            MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(newWorld));
        });
    }

    public static boolean doLevelFileBackup(MinecraftServer server) {
        var levelRoot = server.getWorldPath(LevelResource.ROOT);
        var levelFile = server.getWorldPath(LevelResource.LEVEL_DATA_FILE);

        var formatter = DateTimeFormatter.ofPattern("'cm4-level-'yyyyMMdd-HHmmss'.dat'");
        var timestamp = formatter.format(ZonedDateTime.now());
        try {
            Files.copy(levelFile, levelRoot.resolve(timestamp));
        } catch (IOException e) {
            CompactMachines.LOGGER.error("Failed to backup level.dat file before modification; canceling register dim attempt.");
            return false;
        }

        return true;
    }

}
