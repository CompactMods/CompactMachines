package dev.compactmods.machines.core.capability;

import org.jetbrains.annotations.Nullable;

/**
 * Marker contract added to {@link net.minecraft.server.MinecraftServer} via
 * interface injection (see {@code core/interfaces.json}) and implemented at
 * runtime by {@code MinecraftServerMixin}. Exists purely so consumer code can
 * call {@code server.getCapability(...)} and have {@code javac} resolve it —
 * the actual method bodies live on the mixin, not here, by design: this
 * interface stays free of {@link net.minecraft.server.MinecraftServer}
 * references so it can be injected onto anything else later without dragging
 * server-specific logic along.
 */
public interface IServerCapabilityHolder {

    <T> @Nullable T getCapability(ServerCapability<T, Void> capability);

    <T, C> @Nullable T getCapability(ServerCapability<T, C> capability, @Nullable C context);
}
