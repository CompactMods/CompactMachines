package dev.compactmods.machines.datagen.util;

import dev.compactmods.machines.dimension.Dimension;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.DimensionTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timelines;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * Used for building dimension types, since Mojang hasn't provided one.
 * By default, this pulls all the overworld values. See {@link DimensionType#DEFAULT_OVERWORLD} for more info,
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DimensionTypeBuilder {
    private OptionalLong fixedTime = OptionalLong.empty();
    private boolean hasSkylight = true;
    private boolean hasCeiling = false;
    private boolean ultraWarm = false;
    private boolean natural = true;
    private boolean hasDragonFight = false;
    private double coordinateScale = 1.0D;
    private boolean piglinSafe = false;
    private boolean bedWorks = true;
    private boolean respawnAnchorWorks = false;
    private boolean hasRaids = true;
    private int minY = 0;
    private int height = 256;
    private int logicalHeight = 256;
    private Identifier effectsLocation = BuiltinDimensionTypes.OVERWORLD.identifier();
    private float ambientLight = 0;

    public DimensionTypeBuilder() {
        this.fixedTime = OptionalLong.empty();
    }

    public DimensionTypeBuilder fixedTime(long time) {
        this.fixedTime = OptionalLong.of(time);
        return this;
    }

    public DimensionTypeBuilder skylight(boolean skylight) {
        this.hasSkylight = skylight;
        return this;
    }

    public DimensionTypeBuilder ceiling(boolean hasCeiling) {
        this.hasCeiling = hasCeiling;
        return this;
    }

    public DimensionTypeBuilder ultraWarm(boolean ultraWarm) {
        this.ultraWarm = ultraWarm;
        return this;
    }

    public DimensionTypeBuilder hasDragonFight(boolean hasDragonFight) {
        this.hasDragonFight = hasDragonFight;
        return this;
    }

    /**
     * Whether compasses work in this dimension.
     *
     * @param natural
     * @return
     */
    public DimensionTypeBuilder natural(boolean natural) {
        this.natural = natural;
        return this;
    }

    /**
     * Coordinate scale against overworld. 1 = OVERWORLD, 8 = NETHER.
     *
     * @param coordinateScale
     * @return
     */
    public DimensionTypeBuilder coordScale(float coordinateScale) {
        this.coordinateScale = coordinateScale;
        return this;
    }

    public DimensionTypeBuilder piglinSafe(boolean piglins) {
        this.piglinSafe = piglins;
        return this;
    }

    public DimensionTypeBuilder bedWorks(boolean beds) {
        this.bedWorks = beds;
        return this;
    }

    public DimensionTypeBuilder respawnAnchorWorks(boolean respawn) {
        this.respawnAnchorWorks = respawn;
        return this;
    }

    public DimensionTypeBuilder raids(boolean raids) {
        this.hasRaids = raids;
        return this;
    }

    public DimensionTypeBuilder heightBounds(int min, int height) {
        return heightBounds(min, height, height);
    }

    public DimensionTypeBuilder heightBounds(int min, int height, int logicalHeight) {
        this.minY = min;
        this.height = height;
        this.logicalHeight = logicalHeight;
        return this;
    }

    public DimensionTypeBuilder effects(Identifier effects) {
        this.effectsLocation = effects;
        return this;
    }

    public DimensionTypeBuilder ambientLight(float light) {
        this.ambientLight = light;
        return this;
    }

    public DimensionType build() {
        // TODO: Add MonsterSettings here, right now it copies overworld
        return new DimensionType(true, hasSkylight, hasCeiling,
                hasDragonFight, coordinateScale,
                minY, height, logicalHeight, HolderSet.empty(), ambientLight,
                new DimensionType.MonsterSettings(UniformInt.of(0, 7), 0),
                DimensionType.Skybox.NONE,
                CardinalLighting.Type.DEFAULT,
                EnvironmentAttributeMap.EMPTY,
                HolderSet.empty(),
                Optional.empty());
    }
}
