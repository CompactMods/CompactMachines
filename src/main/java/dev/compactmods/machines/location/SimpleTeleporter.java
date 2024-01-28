package dev.compactmods.machines.location;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record SimpleTeleporter(Vec3 pos, Vec2 rotation) implements ITeleporter {

    public static SimpleTeleporter to(Vec3 pos) {
        return new SimpleTeleporter(pos, Vec2.ZERO);
    }

    public static SimpleTeleporter to(Vec3 pos, Vec2 rotation) {
        return new SimpleTeleporter(pos, rotation);
    }

    @Override
    public @Nullable PortalInfo getPortalInfo(Entity entity, ServerLevel destWorld, Function<ServerLevel, PortalInfo> defaultPortalInfo) {
        return new PortalInfo(pos, Vec3.ZERO, rotation.y, rotation.x);
    }

    @Override
    public Entity placeEntity(Entity entity, ServerLevel currentWorld, ServerLevel destWorld, float yaw, Function<Boolean, Entity> repositionEntity) {
        return repositionEntity.apply(false);
    }
}
