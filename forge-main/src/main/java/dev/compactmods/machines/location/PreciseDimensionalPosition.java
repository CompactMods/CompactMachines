package dev.compactmods.machines.location;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.compactmods.machines.api.location.IDimensionalPosition;
import dev.compactmods.machines.codec.CodecExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public record PreciseDimensionalPosition(ResourceKey<Level> dimension, Vec3 position, Vec2 rotation)
        implements IDimensionalPosition {

    public static final Codec<PreciseDimensionalPosition> CODEC = RecordCodecBuilder.create(i -> i.group(
            ResourceKey.codec(Registry.DIMENSION_REGISTRY).fieldOf("dim").forGetter(PreciseDimensionalPosition::dimension),
            CodecExtensions.VECTOR3D.fieldOf("pos").forGetter(PreciseDimensionalPosition::position),
            CodecExtensions.VEC2.optionalFieldOf("rot", Vec2.ZERO).forGetter(x -> x.rotation)
    ).apply(i, PreciseDimensionalPosition::new));

    public static PreciseDimensionalPosition fromPlayer(Player player) {
        return new PreciseDimensionalPosition(player.level.dimension(), player.position(), new Vec2(player.xRotO, player.yRotO));
    }

    @Override
    public ServerLevel level(MinecraftServer server) {
        return server.getLevel(dimension);
    }

    @Override
    public boolean isLoaded(MinecraftServer serv) {
        return level(serv).isLoaded(new BlockPos(position));
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
