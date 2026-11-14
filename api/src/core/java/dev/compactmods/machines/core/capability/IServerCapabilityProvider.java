package dev.compactmods.machines.core.capability;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public interface IServerCapabilityProvider<T,C> {

    @Nullable T getCapability(MinecraftServer server, @Nullable C context);

}
