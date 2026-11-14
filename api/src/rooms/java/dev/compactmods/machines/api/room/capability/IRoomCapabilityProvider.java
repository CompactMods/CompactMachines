package dev.compactmods.machines.api.room.capability;

import dev.compactmods.machines.api.room.RoomInstance;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IRoomCapabilityProvider<T,C> {
    @Nullable T getCapability(MinecraftServer server, RoomInstance room, @Nullable C context);
}
