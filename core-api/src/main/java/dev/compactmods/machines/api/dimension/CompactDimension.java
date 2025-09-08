package dev.compactmods.machines.api.dimension;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public abstract class CompactDimension {
    public static final ResourceKey<Level> LEVEL_KEY = ResourceKey
            .create(Registries.DIMENSION, CompactMachines.modRL("compact_world"));

    public static final ResourceKey<DimensionType> DIM_TYPE_KEY = ResourceKey
            .create(Registries.DIMENSION_TYPE, CompactMachines.modRL("compact_world"));

    private CompactDimension() {}

    @NotNull
    public static ServerLevel forServer(MinecraftServer server) throws MissingDimensionException {
        final var level = server.getLevel(LEVEL_KEY);
        if(level == null)
            throw new MissingDimensionException();

        return level;
    }

    public static boolean isLevelCompact(Level level) {
        return isLevelCompact(level.dimension());
    }

    public static boolean isLevelCompact(ResourceKey<Level> level) {
        return level.equals(LEVEL_KEY);
    }

    public static boolean isInServerDimension(@NotNull LivingEntity entity) {
        final var l = entity.level();
        return !l.isClientSide() && l.dimension().equals(LEVEL_KEY);
    }
}
