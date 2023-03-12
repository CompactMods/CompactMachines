package dev.compactmods.machines.forge.dimension;

import dev.compactmods.machines.forge.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraftforge.registries.RegistryObject;

import static dev.compactmods.machines.api.core.Constants.MOD_ID;

public class Dimension {

    public static final RegistryObject<Block> BLOCK_MACHINE_VOID_AIR = Registries.BLOCKS.register("machine_void_air", VoidAirBlock::new);

    // ================================================================================================================
    //   DIMENSION
    // ================================================================================================================
    @Deprecated(forRemoval = true)
    public static final ResourceKey<Level> COMPACT_DIMENSION = ResourceKey
            .create(Registry.DIMENSION_REGISTRY, new ResourceLocation(MOD_ID, "compact_world"));

    @Deprecated(forRemoval = true)
    public static final ResourceKey<DimensionType> COMPACT_DIMENSION_DIM_TYPE = ResourceKey
            .create(Registry.DIMENSION_TYPE_REGISTRY, new ResourceLocation(MOD_ID, "compact_world"));

    public static void prepare() {

    }
}
