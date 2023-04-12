package dev.compactmods.machines.forge.tunnel.rotation;

import dev.compactmods.machines.api.tunnels.lifecycle.IPlayerLifecycleEventReason;
import dev.compactmods.machines.api.tunnels.lifecycle.rotation.ITunnelRotationReason;
import net.minecraft.server.level.ServerPlayer;

public record ServerPlayerRotatedReason(ServerPlayer player) implements ITunnelRotationReason, IPlayerLifecycleEventReason {
}
