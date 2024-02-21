package dev.compactmods.machines.neoforge.dimension;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.ITeleporter;

import java.util.Objects;
import java.util.function.Function;

public final class SimpleTeleporter implements ITeleporter {
    private final Vec3 pos;
    private final Vec2 rotation;

    private final BlockPos postTeleLookAt;

    private SimpleTeleporter(Vec3 pos, Vec2 rotation) {
        this.pos = pos;
        this.rotation = rotation;
        this.postTeleLookAt = null;
    }

    private SimpleTeleporter(Vec3 pos, Vec2 rotation, BlockPos postTeleLookAt) {
        this.pos = pos;
        this.rotation = rotation;
        this.postTeleLookAt = postTeleLookAt;
    }

    public static SimpleTeleporter to(Vec3 pos) {
        return new SimpleTeleporter(pos, Vec2.ZERO);
    }

    public static SimpleTeleporter to(Vec3 pos, Vec2 rotation) {
        return new SimpleTeleporter(pos, rotation);
    }

    public static ITeleporter lookingAt(Vec3 position, BlockPos lookAt) {
        return new SimpleTeleporter(position, Vec2.ZERO, lookAt);
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        entity = repositionEntity.apply(false);
        entity.teleportTo(pos.x, pos.y, pos.z);
        entity.setXRot(rotation.x);
        entity.setYRot(rotation.y);
        entity.setYHeadRot(rotation.y);

        if(postTeleLookAt != null)
            entity.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(postTeleLookAt));

        return entity;
    }

    public Vec3 pos() {
        return pos;
    }

    public Vec2 rotation() {
        return rotation;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SimpleTeleporter) obj;
        return Objects.equals(this.pos, that.pos) &&
                Objects.equals(this.rotation, that.rotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, rotation);
    }

    @Override
    public String toString() {
        return "SimpleTeleporter[" +
                "pos=" + pos + ", " +
                "rotation=" + rotation + ']';
    }

}
