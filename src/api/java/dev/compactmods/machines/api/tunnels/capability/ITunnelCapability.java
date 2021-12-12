package dev.compactmods.machines.api.tunnels.capability;

/**
 * Marker interface to combine {@link ITunnelCapabilitySetup} and {@link ITunnelCapabilityTeardown}.
 */
public interface ITunnelCapability extends ITunnelCapabilitySetup, ITunnelCapabilityTeardown {
}
