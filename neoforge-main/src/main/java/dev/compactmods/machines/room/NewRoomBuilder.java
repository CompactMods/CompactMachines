package dev.compactmods.machines.room;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.registration.IRoomBuilder;
import dev.compactmods.machines.api.room.spatial.IRoomBoundaries;
import dev.compactmods.machines.machine.MachineColors;
import dev.compactmods.machines.room.graph.node.RoomRegistrationNode;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class NewRoomBuilder implements IRoomBuilder {
    private final String code;
    private MachineColor color = MachineColors.WHITE;

    private AABB boundaries = AABB.ofSize(Vec3.ZERO, 1, 1, 1);
    UUID owner;

    public NewRoomBuilder() {
        this.code = RoomCodeGenerator.generateRoomId();
    }

    public NewRoomBuilder boundaries(AABB boundaries) {
        this.boundaries = boundaries;
        return this;
    }

    public NewRoomBuilder offsetCenter(Vec3 offset) {
        this.boundaries = this.boundaries.move(offset);
        return this;
    }

    public NewRoomBuilder owner(UUID owner) {
        this.owner = owner;
        return this;
    }

    public NewRoomBuilder defaultMachineColor(int color) {
        this.color = MachineColor.fromARGB(color);
        return this;
    }

    public NewRoomBuilder defaultMachineColor(MachineColor color) {
        this.color = color;
        return this;
    }

    public RoomInstance build(MinecraftServer server) {
        return new RoomInstance(server, CompactDimension.LEVEL_KEY, code, color, () -> boundaries);
    }
}
