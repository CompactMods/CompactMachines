package dev.compactmods.machines.test.util;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.neoforge.dimension.Dimension;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

/*
    Mostly yoink'd from RF Tools for 1.20, and our datagen. Going to clean this up and reduce more
    if there are ways to shrink this down.
 */
public class DimensionForcer {

    public static LevelStem makeStem(RegistryAccess registryAccess) {
        final var biomes = registryAccess.lookup(Registries.BIOME).orElseThrow();
        final var dimTypes = registryAccess.lookup(Registries.DIMENSION_TYPE).orElseThrow();

        final var cmBiome = biomes.getOrThrow(ResourceKey.create(Registries.BIOME, new ResourceLocation(Constants.MOD_ID, "machine")));

        var flatSettings = new FlatLevelGeneratorSettings(Optional.empty(), cmBiome, Collections.emptyList())
                .withBiomeAndLayers(
                        List.of(new FlatLayerInfo(48, Dimension.BLOCK_MACHINE_VOID_AIR.get())),
                        Optional.empty(),
                        cmBiome
                );

        return new LevelStem(dimTypes.getOrThrow(CompactDimension.DIM_TYPE_KEY), new FlatLevelSource(flatSettings));
    }

    public static void forceLoadCMDim(MinecraftServer server) {
        // get everything we need to create the dimensionStem and the level
        final ServerLevel overworld = server.getLevel(Level.OVERWORLD);

        // dimensionStem keys have a 1:1 relationship with level keys, they have the same IDs as well
        final ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registries.LEVEL_STEM, CompactDimension.LEVEL_KEY.location());
        final LevelStem dimensionStem = makeStem(server.registryAccess());

        // the int in create() here is radius of chunks to watch, 11 is what the server uses when it initializes worlds
        final ChunkProgressListener chunkProgressListener = server.progressListenerFactory.create(11);
        final Executor executor = server.executor;
        final LevelStorageSource.LevelStorageAccess anvilConverter = server.storageSource;
        final WorldData worldData = server.getWorldData();
        final WorldOptions worldGenSettings = worldData.worldGenOptions();
        final DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());

        // now we have everything we need to create the dimensionStem and the level
        // this is the same order server init creates levels:
        // the dimensions are already registered when levels are created, we'll do that first
        // then instantiate level, add border listener, add to map, fire world load event

        // register the actual dimensionStem
        LayeredRegistryAccess<RegistryLayer> registries = server.registries();
        RegistryAccess.ImmutableRegistryAccess composite = (RegistryAccess.ImmutableRegistryAccess)registries.compositeAccess();

        Map<ResourceKey<? extends Registry<?>>, Registry<?>> regmap = new HashMap<>(composite.registries);
        ResourceKey<? extends Registry<?>> key = ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation("root")),new ResourceLocation("dimension"));
        MappedRegistry<LevelStem> oldRegistry = (MappedRegistry<LevelStem>) regmap.get(key);
        Lifecycle oldLifecycle = oldRegistry.registryLifecycle();

        final MappedRegistry<LevelStem> newRegistry = new MappedRegistry<>(Registries.LEVEL_STEM, oldLifecycle, false);
        for (var entry : oldRegistry.entrySet()) {
            final ResourceKey<LevelStem> oldKey = entry.getKey();
            final ResourceKey<Level> oldLevelKey = ResourceKey.create(Registries.DIMENSION, oldKey.location());
            final LevelStem dim = entry.getValue();
            if (dim != null && oldLevelKey != CompactDimension.LEVEL_KEY) {
                Registry.register(newRegistry, oldKey, dim);
            }
        }

        Registry.register(newRegistry, dimensionKey, dimensionStem);
        regmap.replace(key, newRegistry);

        Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> newmap = regmap;
        composite.registries = newmap;

        // create the world instance
        final ServerLevel newWorld = new ServerLevel(
                server,
                executor,
                anvilConverter,
                derivedLevelData,
                CompactDimension.LEVEL_KEY,
                dimensionStem,
                chunkProgressListener,
                false,
                net.minecraft.world.level.biome.BiomeManager.obfuscateSeed(worldGenSettings.seed()),
                ImmutableList.of(),
                false,
                null // @todo 1.20, what is this?
        );

        // register level
        server.forgeGetWorldMap().put(CompactDimension.LEVEL_KEY, newWorld);

        // update forge's world cache so the new level can be ticked
        server.markWorldsDirty();
    }
}
