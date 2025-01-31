package dev.compactmods.machines.api.room.capability;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IRoomCapabilityProvider<T,C> {
    @Nullable T getCapability(MinecraftServer server, String roomCode, @Nullable C context);
}
