package dev.compactmods.machines.datagen;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiConsumer;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import dev.compactmods.machines.CompactMachines;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;

public class LevelBiomeGenerator implements DataProvider {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final DataGenerator generator;

    private final ResourceLocation COMPACT_BIOME = new ResourceLocation(CompactMachines.MOD_ID, "machine");

    LevelBiomeGenerator(DataGenerator generator) {
        this.generator = generator;
    }
    
    @Override
    public void run(@NotNull HashCache cache) {
        Path data = this.generator.getOutputFolder();
        Set<ResourceLocation> set = Sets.newHashSet();

        var biomeWriter = makeWriter(cache, data, ImmutableSet.of("worldgen", "biome"), Biome.DIRECT_CODEC, set);
        // var dimWriter = makeWriter(cache, data, ImmutableSet.of("dimension"), LevelStem.CODEC, set);
        var dimTypeWriter = makeWriter(cache, data, ImmutableSet.of("dimension_type"), DimensionType.DIRECT_CODEC, set);

        writeBiomes(biomeWriter);
        // writeDimensions(dimWriter);
        writeDimensionTypes(dimTypeWriter);
    }

    private void writeDimensionTypes(BiConsumer<DimensionType, ResourceLocation> consumer) {

        final DimensionType dim = new DimensionTypeBuilder()
                .bedWorks(false)
                .respawnAnchorWorks(false)
                .fixedTime(18000L)
                .natural(false)
                .raids(false)
                .heightBounds(0, 256)
                .build();

        consumer.accept(dim, new ResourceLocation(CompactMachines.MOD_ID, "compact_world"));
    }

    private void writeBiomes(BiConsumer<Biome, ResourceLocation> biomeWriter) {
        var spawnBuilder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.plainsSpawns(spawnBuilder);
        var spawns = spawnBuilder.build();

        final Biome compactBiome = new Biome.BiomeBuilder()
                .biomeCategory(Biome.BiomeCategory.NONE)
                .downfall(0)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(spawns)
                .precipitation(Biome.Precipitation.NONE)
                .temperature(0.8f)
                .temperatureAdjustment(Biome.TemperatureModifier.NONE)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(12638463)
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .skyColor(0xFF000000)
                        .build())
                .build();

        compactBiome.setRegistryName(COMPACT_BIOME);
        biomeWriter.accept(compactBiome, COMPACT_BIOME);
    }

    private <T> BiConsumer<T, ResourceLocation> makeWriter(@NotNull HashCache cache, Path dataDir, ImmutableSet<String> pathParts, Codec<T> codec, Set<ResourceLocation> set) {
        return (T resource, ResourceLocation regName) -> {
            if (!set.add(regName)) {
                throw new IllegalStateException("Duplicate resource " + regName);
            } else {
                String namespace = regName.getNamespace();
                String path = regName.getPath();

                Path fileLocation = dataDir.resolve(Path.of("data", CompactMachines.MOD_ID));
                for(String p : pathParts)
                        fileLocation = fileLocation.resolve(p);

                fileLocation = fileLocation.resolve(path + ".json");

                try {
                    //noinspection OptionalGetWithoutIsPresent
                    DataProvider.save(GSON, cache, codec.encodeStart(JsonOps.INSTANCE, resource).result().get(), fileLocation);

                } catch (IOException ioe) {
                    CompactMachines.LOGGER.error("Couldn't save resource {}", fileLocation, ioe);
                }

            }
        };
    }

    @Override
    public String getName() {
        return CompactMachines.MOD_ID + ":levelgen";
    }
}
