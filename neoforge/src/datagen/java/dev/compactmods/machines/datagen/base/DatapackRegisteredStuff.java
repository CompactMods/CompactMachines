package dev.compactmods.machines.datagen.base;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.machine.client.shader.flag.FlagShader;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.datagen.util.DimensionTypeBuilder;
import dev.compactmods.machines.dimension.Dimension;
import dev.compactmods.machines.upgrades.api.system.CompiledRoomUpgrade;
import dev.compactmods.machines.upgrades.example.TreeCutterUpgradeNode;
import dev.robotgryphon.screenlib.graph.schema.NodeDefinition;
import dev.compactmods.machines.upgrades.example.ChunkLoaderUpgradeComponent;
import dev.compactmods.machines.upgrades.example.TreeCutterUpgradeComponent;
import dev.compactmods.machines.upgrades.storage.ResourceStorageTypes;
import dev.compactmods.machines.upgrades.storage.ResourceTypes;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelTypes;
import dev.compactmods.machines.upgrades.storage.nodes.StorageNodes;
import dev.compactmods.machines.machine.config.MachineSideNode;
import net.minecraft.world.item.Items;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.attribute.EnvironmentAttributes;
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
            .add(RoomTemplate.REGISTRY_KEY, (ctx) -> {
            })
            .add(FlagShader.REGISTRY_KEY, DatapackRegisteredStuff::generateFlagDefinitions)
            .add(CompiledRoomUpgrade.REGISTRY_KEY, DatapackRegisteredStuff::generateCompiledRoomUpgrades)
            .add(NodeDefinition.REGISTRY_KEY, DatapackRegisteredStuff::generateNodeDefinitions)
            .add(TunnelType.REGISTRY_KEY, DatapackRegisteredStuff::generateTunnelTypes);

    private static void generateTunnelTypes(BootstrapContext<TunnelType> ctx) {
        ctx.register(TunnelTypes.ITEM, new TunnelType(
                ResourceTypes.ITEM.getKey(), ResourceStorageTypes.ITEM_BLOCK.getKey(),
                0xFFB300, CompactMachinesCore.identifier("tunnels/item"),
                Items.CHEST.builtInRegistryHolder(), 9));

        ctx.register(TunnelTypes.FLUID, new TunnelType(
                ResourceTypes.FLUID.getKey(), ResourceStorageTypes.FLUID_BLOCK.getKey(),
                0x2196F3, CompactMachinesCore.identifier("tunnels/fluid"),
                Items.BUCKET.builtInRegistryHolder(), 4));
    }

    /** Screenlib canvas nodes for the room upgrade canvas (see {@code RoomCanvases}). */
    private static void generateNodeDefinitions(BootstrapContext<NodeDefinition> ctx) {
        ctx.register(TreeCutterUpgradeNode.KEY, TreeCutterUpgradeNode.definition());
        ctx.register(StorageNodes.NAMED_ITEM_STORAGE, StorageNodes.namedItemStorage());
        ctx.register(StorageNodes.NAMED_FLUID_STORAGE, StorageNodes.namedFluidStorage());
        ctx.register(MachineSideNode.KEY, MachineSideNode.definition());
    }

    private static void generateCompiledRoomUpgrades(BootstrapContext<CompiledRoomUpgrade> ctx) {
        ctx.register(
                ResourceKey.create(CompiledRoomUpgrade.REGISTRY_KEY, CompactMachinesCore.identifier("tree_cutter")),
                new CompiledRoomUpgrade(8, 1, 200, List.of(new TreeCutterUpgradeComponent())));

        ctx.register(
                ResourceKey.create(CompiledRoomUpgrade.REGISTRY_KEY, CompactMachinesCore.identifier("chunk_loader")),
                new CompiledRoomUpgrade(8, 1, 200, List.of(new ChunkLoaderUpgradeComponent())));
    }

    private static void generateFlagDefinitions(BootstrapContext<FlagShader> ctx) {
        generateFlag(ctx, "baker", 0xE50000, 0xFF8D00, 0xFFEE00,
                0x028121, 0x004CFF, 0x770088);

        generateFlag(ctx, "lesbian", 0xD62800, 0xFF9B56, 0xFFFFFF, 0xD462A6, 0xA40062);

        generateFlag(ctx, "bisexual", 0xD60270, 0x9BF96, 0x0038A8);

        generateFlag(ctx, "asexual", 0x000000, 0xA4A4A4, 0xFFFFFF, 0x810081);

        generateFlag(ctx, "transgender", 0x5BCFFB, 0xF5ABB9, 0xFFFFFF, 0xF5ABB9, 0x5BCFFB);

        generateFlag(ctx, "non-binary", 0xFCF431, 0xFCFCFC, 0x9D59D2, 0x282828);
    }

    private static void generateFlag(BootstrapContext<FlagShader> ctx, String id, Integer... colors) {
        final var definition = new FlagShader(List.of(colors));
        ctx.register(ResourceKey.create(FlagShader.REGISTRY_KEY, CompactMachinesCore.identifier("pride/" + id)), definition);
    }

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
                        .waterColor(4159204)
                        .build())
                .setAttribute(EnvironmentAttributes.FOG_COLOR, ARGB.vector3fFromRGB24(12638463))
                .setAttribute(EnvironmentAttributes.WATER_FOG_COLOR, ARGB.vector3fFromRGB24(329011))
                .setAttribute(EnvironmentAttributes.SKY_COLOR, ARGB.vector3fFromRGB24(0xFF000000))
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
        ctx.register(ResourceKey.create(Registries.LEVEL_STEM, CompactDimension.LEVEL_KEY.identifier()), stem);
    }
}
