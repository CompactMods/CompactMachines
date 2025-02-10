package dev.compactmods.machines.api.server;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public interface ServerServiceProvider<T> {
    @NotNull
    T makeServiceInstance(MinecraftServer server);
}
