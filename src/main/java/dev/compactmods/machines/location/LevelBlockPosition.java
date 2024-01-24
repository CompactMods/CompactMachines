package dev.compactmods.machines.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.api.location.IDimensionalBlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

public class LevelBlockPosition implements INBTSerializable<CompoundTag>, IDimensionalBlockPosition {

    private ResourceKey<Level> dimension;
    private Vec3 position;
    private Vec3 rotation;

    public static final Codec<LevelBlockPosition> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).fieldOf("dim").forGetter(LevelBlockPosition::getDimension),
            CodecExtensions.VECTOR3D.fieldOf("pos").forGetter(LevelBlockPosition::getExactPosition),
            CodecExtensions.VECTOR3D.optionalFieldOf("rot", Vec3.ZERO).forGetter(x -> x.rotation)
    ).apply(i, LevelBlockPosition::new));

    private LevelBlockPosition() {
    }

    public LevelBlockPosition(IDimensionalBlockPosition base) {
        this.dimension = base.dimensionKey();
        this.position = base.getExactPosition();
        this.rotation = Vec3.ZERO;
    }

    public LevelBlockPosition(ResourceKey<Level> world, BlockPos positionBlock) {
        this(world, Vec3.ZERO, Vec3.ZERO);
        this.position = new Vec3(positionBlock.getX(), positionBlock.getY(), positionBlock.getZ());
        this.rotation = Vec3.ZERO;
    }

    public LevelBlockPosition(ResourceKey<Level> world, Vec3 positionBlock) {
        this(world, positionBlock, Vec3.ZERO);
        this.dimension = world;
        this.rotation = Vec3.ZERO;
    }

    public LevelBlockPosition(ResourceKey<Level> dim, Vec3 pos, Vec3 rotation) {
        this.dimension = dim;
        this.position = pos;
        this.rotation = rotation;
    }

    public static LevelBlockPosition fromEntity(LivingEntity entity) {
        return new LevelBlockPosition(entity.level.dimension(), entity.position());
    }

    public ServerLevel level(@Nonnull MinecraftServer server) {
        return server.getLevel(this.dimension);
    }

    public BlockState state(MinecraftServer server) {
        final var level = level(server);
        return level.getBlockState(getBlockPosition());
    }

    @Override
    public IDimensionalBlockPosition relative(Direction direction) {
        return new LevelBlockPosition(this.dimension, this.position.add(direction.getStepX(), direction.getStepY(), direction.getStepZ()));
    }

    public boolean isLoaded(MinecraftServer server) {
        final var level = level(server);
        return level.isLoaded(new BlockPos(position));
    }

    public static LevelBlockPosition fromNBT(CompoundTag nbt) {
        LevelBlockPosition dp = new LevelBlockPosition();
        dp.deserializeNBT(nbt);

        return dp;
    }

    @Override
    public CompoundTag serializeNBT() {
        if(this.rotation == null)
            this.rotation = Vec3.ZERO;

        DataResult<Tag> nbt = CODEC.encodeStart(NbtOps.INSTANCE, this);
        return (CompoundTag) nbt.result().orElse(null);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        Optional<LevelBlockPosition> dimensionalPosition = CODEC
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

    public Optional<Vec3> getRotation() {
        return Optional.of(this.rotation);
    }

    public BlockPos getBlockPosition() {
        return new BlockPos(position.x, position.y, position.z);
    }

    @Override
    public Vec3 getExactPosition() {
        return this.position;
    }

    @Override
    public ResourceKey<Level> dimensionKey() {
        return dimension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        LevelBlockPosition that = (LevelBlockPosition) o;
        if (!dimension.equals(that.dimension))
            return false;

        if (!position.equals(that.position))
            return false;

        return rotation.equals(that.rotation);
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
