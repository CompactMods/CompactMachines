package dev.compactmods.machines.api.room.registration;

import dev.compactmods.machines.api.machine.MachineColor;
import dev.compactmods.machines.api.room.RoomInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.UUID;

public interface IRoomBuilder {

    IRoomBuilder boundaries(AABB boundaries);

    IRoomBuilder owner(UUID owner);

    IRoomBuilder defaultMachineColor(MachineColor color);

    default RoomInstance build() {
        return build(ServerLifecycleHooks.getCurrentServer());
    }

    RoomInstance build(MinecraftServer server);
}
