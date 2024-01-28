package dev.compactmods.machines.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.codec.CodecExtensions;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.Optional;

public final class PreciseDimensionalPosition implements IDimensionalPosition {

    public static final Codec<PreciseDimensionalPosition> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).fieldOf("dim").forGetter(PreciseDimensionalPosition::dimension),
            CodecExtensions.VECTOR3D.fieldOf("pos").forGetter(PreciseDimensionalPosition::position),
            CodecExtensions.VECTOR3D.optionalFieldOf("rot", Vec3.ZERO).forGetter(x -> x.rotation)
    ).apply(i, PreciseDimensionalPosition::new));

    private final ResourceKey<Level> dimension;
    private final Vec3 position;
    private final Vec3 rotation;

    public PreciseDimensionalPosition(ResourceKey<Level> dimension, Vec3 position) {
        this.dimension = dimension;
        this.position = position;
        this.rotation = Vec3.ZERO;
    }

    public PreciseDimensionalPosition(ResourceKey<Level> dimension, Vec3 position, Vec3 rotation) {
        this.dimension = dimension;
        this.position = position;
        this.rotation = rotation;
    }

    public static PreciseDimensionalPosition fromPlayer(Player player) {
        return new PreciseDimensionalPosition(player.level.dimension(), player.position(), player.getLookAngle());
    }

    @Override
    public BlockPos getBlockPosition() {
        return new BlockPos(position.x, position.y, position.z);
    }

    @Override
    public Vec3 getExactPosition() {
        return position;
    }

   public Optional<Vec3> rotation() {
        return Optional.ofNullable(rotation);
    }

    @Override
    public ResourceKey<Level> dimensionKey() {
        return dimension;
    }

    @Override
    public ServerLevel level(MinecraftServer server) {
        return server.getLevel(dimension);
    }

    @Override
    public IDimensionalPosition relative(Direction direction) {
        final var newPos = position.add(direction.getStepX(), direction.getStepY(), direction.getStepZ());
        return new PreciseDimensionalPosition(dimension, newPos);
    }

    @Override
    public Optional<Vec2> getRotation() {
        return Optional.empty();
    }

    @Override
    public boolean isLoaded(MinecraftServer serv) {
        return level(serv).isLoaded(new BlockPos(position));
    }

    public ResourceKey<Level> dimension() {
        return dimension;
    }

    public Vec3 position() {
        return position;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PreciseDimensionalPosition) obj;
        return Objects.equals(this.dimension, that.dimension) &&
                Objects.equals(this.position, that.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, position);
    }

    @Override
    public String toString() {
        return "PreciseDimensionalPosition[" +
                "dimension=" + dimension + ", " +
                "position=" + position + ']';
    }

}
