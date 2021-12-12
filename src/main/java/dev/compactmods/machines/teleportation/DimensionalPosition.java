package dev.compactmods.machines.teleportation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.data.codec.CodecExtensions;
import dev.compactmods.machines.util.LocationUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public class DimensionalPosition implements INBTSerializable<CompoundTag>, IDimensionalPosition {

    private ResourceKey<Level> dimension;
    private Vec3 position;
    private Vec3 rotation;

    /*
     Note: We'd use the actual world registry key here, but it static loads the world and does a bunch
     of initialization, making it impossible to unit test without booting a whole server up.
    */
    public static final Codec<DimensionalPosition> CODEC = RecordCodecBuilder.create(i -> i.group(
            CodecExtensions.WORLD_REGISTRY_KEY.fieldOf("dim").forGetter(DimensionalPosition::getDimension),
            CodecExtensions.VECTOR3D.fieldOf("position").forGetter(DimensionalPosition::getPosition),
            CodecExtensions.VECTOR3D.optionalFieldOf("rot", Vec3.ZERO).forGetter(DimensionalPosition::getRotation)
    ).apply(i, DimensionalPosition::new));

    private DimensionalPosition() {
    }

    public DimensionalPosition(ResourceKey<Level> world, BlockPos positionBlock) {
        this(world, Vec3.ZERO, Vec3.ZERO);
        this.position = new Vec3(positionBlock.getX(), positionBlock.getY(), positionBlock.getZ());
    }

    public DimensionalPosition(ResourceKey<Level> world, Vec3 positionBlock) {
        this(world, positionBlock, Vec3.ZERO);
        this.dimension = world;

        this.rotation = Vec3.ZERO;
    }

    public DimensionalPosition(ResourceKey<Level> dim, Vec3 pos, Vec3 rotation) {
        this.dimension = dim;
        this.position = pos;
        this.rotation = rotation;
    }

    public static DimensionalPosition fromEntity(LivingEntity entity) {
        return new DimensionalPosition(entity.level.dimension(), entity.position());
    }

    public Optional<ServerLevel> getWorld(@Nonnull MinecraftServer server) {
        return Optional.ofNullable(server.getLevel(this.dimension));
    }

    public boolean isLoaded(MinecraftServer server) {
        return getWorld(server)
                .map(w -> w.isLoaded(LocationUtil.vectorToBlockPos(position)))
                .orElse(false);
    }

    public static DimensionalPosition fromNBT(CompoundTag nbt) {
        DimensionalPosition dp = new DimensionalPosition();
        dp.deserializeNBT(nbt);

        return dp;
    }

    @Override
    public CompoundTag serializeNBT() {
        DataResult<Tag> nbt = CODEC.encodeStart(NbtOps.INSTANCE, this);
        return (CompoundTag) nbt.result().orElse(null);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        Optional<DimensionalPosition> dimensionalPosition = CODEC
                .parse(NbtOps.INSTANCE, nbt)
                .resultOrPartial(CompactMachines.LOGGER::error);

        dimensionalPosition.ifPresent(dp -> {
            this.dimension = dp.dimension;
            this.position = dp.position;
            this.rotation = dp.rotation;
        });
    }

    public ResourceKey<Level> getDimension() {
        return this.dimension;
    }

    public Vec3 getPosition() {
        return this.position;
    }

    public Vec3 getRotation() {
        return this.rotation;
    }

    public BlockPos getBlockPosition() {
        return new BlockPos(position.x, position.y, position.z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DimensionalPosition that = (DimensionalPosition) o;
        return Objects.equals(dimension, that.dimension) &&
                Objects.equals(position, that.position) &&
                Objects.equals(rotation, rotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, position, rotation);
    }

    @Override
    public String toString() {
        return "DimensionalPosition{" +
                "d=" + dimension +
                ", p=" + position +
                ", r=" + rotation +
                '}';
    }
}
