package dev.compactmods.machines.command.argument;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

public class RoomCoordinates {
    private final WorldCoordinate x;
    private final WorldCoordinate z;

    public RoomCoordinates(WorldCoordinate x, WorldCoordinate z) {
        this.x = x;
        this.z = z;
    }

    public ChunkPos get(CommandSourceStack stack) {
        Vec3 vec3 = stack.getPosition();
        return new ChunkPos((int) this.x.get(vec3.x), (int) this.z.get(vec3.z));
    }
}
