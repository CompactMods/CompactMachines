package dev.compactmods.machines.room.graph;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class NewRoomBuilder {
    private final String code;
    private int color = 0;
    private Vec3i dimensions = Vec3i.ZERO;
    private Vec3 center = Vec3.ZERO;
    private Vec3 centerOffset = Vec3.ZERO;
    private Vec3 spawn = Vec3.ZERO;
    private Vec2 spawnRotation = Vec2.ZERO;
    UUID owner;

    NewRoomBuilder(String newRoomCode) {
        this.code = newRoomCode;
    }

    public NewRoomBuilder setDimensions(Vec3i dimensions) {
        this.dimensions = dimensions;
        return this;
    }

    public NewRoomBuilder offsetCenter(Vec3 offset) {
        this.centerOffset = offset;
        return this;
    }

    public NewRoomBuilder setOwner(UUID owner) {
        this.owner = owner;
        return this;
    }

    public NewRoomBuilder setColor(int color) {
        this.color = color;
        return this;
    }

    int yOffset() {
        return Math.floorDiv(dimensions.getY(), 2);
    }

    public NewRoomBuilder setCenter(BlockPos newCenter) {
        this.center = Vec3.atCenterOf(newCenter);
        this.spawn = Vec3.atCenterOf(newCenter);
        this.spawnRotation = Vec2.ZERO;
        return this;
    }

    public NewRoomBuilder setSpawn(Vec3 spawn, Vec2 spawnRotation) {
        this.spawn = spawn;
        this.spawnRotation = spawnRotation;
        return this;
    }

    RoomMetadataNode build() {
        return new RoomMetadataNode(code, color, dimensions,
                center.add(centerOffset),
                spawn,
                spawnRotation);
    }
}
