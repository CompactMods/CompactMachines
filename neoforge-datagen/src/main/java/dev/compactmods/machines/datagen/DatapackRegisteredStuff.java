package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.room.RoomTemplate;
import dev.compactmods.machines.api.Constants;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.datagen.util.DimensionTypeBuilder;
import dev.compactmods.machines.neoforge.dimension.Dimension;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DatapackRegisteredStuff extends DatapackBuiltinEntriesProvider {
    private static final ResourceLocation COMPACT_BIOME = new ResourceLocation(Constants.MOD_ID, "machine");
    private static final int DIMENSION_HEIGHT = 48;

    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.BIOME, DatapackRegisteredStuff::generateBiomes)
            .add(Registries.DIMENSION_TYPE, DatapackRegisteredStuff::generateDimensionTypes)
            .add(Registries.LEVEL_STEM, DatapackRegisteredStuff::generateDimensions)
            .add(RoomTemplate.REGISTRY_KEY, DatapackRegisteredStuff::addRoomTemplates);

    DatapackRegisteredStuff(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries, BUILDER, Set.of(Constants.MOD_ID));
    }

    private static void generateBiomes(BootstapContext<Biome> ctx) {
        var spawnBuilder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.plainsSpawns(spawnBuilder);
        var spawns = spawnBuilder.build();

        final Biome compactBiome = new Biome.BiomeBuilder()
                .downfall(0)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .mobSpawnSettings(spawns)
                .hasPrecipitation(false)
                .temperature(0.8f)
                .temperatureAdjustment(Biome.TemperatureModifier.NONE)
                .specialEffects(new BiomeSpecialEffects.Builder()
                        .fogColor(12638463)
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .skyColor(0xFF000000)
                        .build())
                .build();

        ctx.register(ResourceKey.create(Registries.BIOME, COMPACT_BIOME), compactBiome);
    }

    private static void generateDimensionTypes(BootstapContext<DimensionType> ctx) {
        ctx.register(CompactDimension.DIM_TYPE_KEY, new DimensionTypeBuilder()
                .bedWorks(false)
                .respawnAnchorWorks(false)
                .fixedTime(18000L)
                .natural(false)
                .raids(false)
                .heightBounds(0, DIMENSION_HEIGHT)
                .build());
    }

    private static void generateDimensions(BootstapContext<LevelStem> ctx) {
        final var biomes = ctx.lookup(Registries.BIOME);
        final var dimTypes = ctx.lookup(Registries.DIMENSION_TYPE);

        final var cmBiome = biomes.getOrThrow(ResourceKey.create(Registries.BIOME, COMPACT_BIOME));

        var flatSettings = new FlatLevelGeneratorSettings(Optional.empty(), cmBiome, Collections.emptyList())
                .withBiomeAndLayers(
                        List.of(new FlatLayerInfo(DIMENSION_HEIGHT, Dimension.BLOCK_MACHINE_VOID_AIR.get())),
                        Optional.empty(),
                        cmBiome
                );

        var stem = new LevelStem(dimTypes.getOrThrow(CompactDimension.DIM_TYPE_KEY), new FlatLevelSource(flatSettings));
        ctx.register(ResourceKey.create(Registries.LEVEL_STEM, CompactDimension.LEVEL_KEY.location()), stem);
    }

    private static void addRoomTemplates(BootstapContext<RoomTemplate> ctx) {
        roomTemplate(ctx, "tiny",       new RoomTemplate(3, FastColor.ARGB32.color(255, 201, 91, 19)));
        roomTemplate(ctx, "small",      new RoomTemplate(5, FastColor.ARGB32.color(255, 212, 210, 210)));
        roomTemplate(ctx, "normal",     new RoomTemplate(7, FastColor.ARGB32.color(255, 251, 242, 54)));
        roomTemplate(ctx, "large",      new RoomTemplate(9, FastColor.ARGB32.color(255, 33, 27, 46)));
        roomTemplate(ctx, "giant",      new RoomTemplate(11, FastColor.ARGB32.color(255, 67, 214, 205)));
        roomTemplate(ctx, "colossal",   new RoomTemplate(13, FastColor.ARGB32.color(255, 66, 63, 66)));
    }

    private static void roomTemplate(BootstapContext<RoomTemplate> ctx, String name, RoomTemplate template) {
        ctx.register(ResourceKey.create(RoomTemplate.REGISTRY_KEY, new ResourceLocation(Constants.MOD_ID, name)), template);
    }
}
