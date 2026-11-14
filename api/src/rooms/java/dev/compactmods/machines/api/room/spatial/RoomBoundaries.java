package dev.compactmods.machines.api.room.spatial;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.stream.Stream;

public record RoomBoundaries(AABB outerBounds) {

    public static MapCodec<RoomBoundaries> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Vec3.CODEC.fieldOf("dimensions").forGetter(RoomBoundaries::dimensions),
            Vec3.CODEC.fieldOf("center").forGetter((RoomBoundaries x) -> x.innerBounds().getCenter())
    ).apply(i, RoomBoundaries::new));

    private RoomBoundaries(Vec3 size, Vec3 center) {
        this(AABB.ofSize(center, size.x, size.y, size.z));
    }

    private Vec3 dimensions() {
        return new Vec3(outerBounds.getXsize(), outerBounds.getYsize(), outerBounds.getZsize());
    }

    public AABB innerBounds() {
        return outerBounds.deflate(1);
    }

    public Stream<ChunkPos> innerChunkPositions() {
        final var ib = innerBounds();
        final var min = ChunkPos.containing(BlockPos.containing(ib.getMinPosition()));
        final var max = ChunkPos.containing(BlockPos.containing(ib.getMaxPosition()));
        return ChunkPos.rangeClosed(min, max);
    }

    public Vec3 defaultSpawn() {
        var newFloorCenter = BlockPos.containing(innerBounds().getCenter()).mutable();
        newFloorCenter.setY((int) (innerBounds().minY + 1));
        return Vec3.atBottomCenterOf(newFloorCenter);
    }
}
