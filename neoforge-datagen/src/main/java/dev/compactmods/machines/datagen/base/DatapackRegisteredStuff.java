package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.datagen.util.DimensionTypeBuilder;
import dev.compactmods.machines.dimension.Dimension;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class DatapackRegisteredStuff {

	private static final int DIMENSION_HEIGHT = 48;

	public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
		.add(Registries.BIOME, DatapackRegisteredStuff::generateBiomes)
		.add(Registries.DIMENSION_TYPE, DatapackRegisteredStuff::generateDimensionTypes)
		.add(Registries.LEVEL_STEM, DatapackRegisteredStuff::generateDimensions)
		.add(RoomTemplate.REGISTRY_KEY, (ctx) -> {});

	private static void generateBiomes(BootstrapContext<Biome> ctx) {
		var spawnBuilder = new MobSpawnSettings.Builder();
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

		ctx.register(ResourceKey.create(Registries.BIOME, CompactDimension.COMPACT_BIOME), compactBiome);
	}

	private static void generateDimensionTypes(BootstrapContext<DimensionType> ctx) {
		ctx.register(CompactDimension.DIM_TYPE_KEY, new DimensionTypeBuilder()
			.bedWorks(false)
			.respawnAnchorWorks(false)
			.fixedTime(18000L)
			.natural(false)
			.raids(false)
			.heightBounds(0, DIMENSION_HEIGHT)
			.build());
	}

	private static void generateDimensions(BootstrapContext<LevelStem> ctx) {
		final var biomes = ctx.lookup(Registries.BIOME);
		final var dimTypes = ctx.lookup(Registries.DIMENSION_TYPE);

		final var cmBiome = biomes.getOrThrow(ResourceKey.create(Registries.BIOME, CompactDimension.COMPACT_BIOME));

		var flatSettings = new FlatLevelGeneratorSettings(Optional.empty(), cmBiome, Collections.emptyList())
			.withBiomeAndLayers(
				List.of(new FlatLayerInfo(DIMENSION_HEIGHT, Dimension.BLOCK_MACHINE_VOID_AIR.get())),
				Optional.empty(),
				cmBiome
			);

		var stem = new LevelStem(dimTypes.getOrThrow(CompactDimension.DIM_TYPE_KEY), new FlatLevelSource(flatSettings));
		ctx.register(ResourceKey.create(Registries.LEVEL_STEM, CompactDimension.LEVEL_KEY.location()), stem);
	}
}
