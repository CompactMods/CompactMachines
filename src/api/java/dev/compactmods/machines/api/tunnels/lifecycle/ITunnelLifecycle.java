package dev.compactmods.machines.api.tunnels.lifecycle;

/**
 * Marker interface to combine {@link ITunnelSetup} and {@link ITunnelTeardown}.
 */
public interface ITunnelLifecycle extends ITunnelSetup, ITunnelTeardown {
}
