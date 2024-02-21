package dev.compactmods.machines.neoforge.tunnel.removal;

import dev.compactmods.machines.api.tunnels.lifecycle.IPlayerLifecycleEventReason;
import dev.compactmods.machines.api.tunnels.lifecycle.removal.ITunnelRemoveReason;
import net.minecraft.server.level.ServerPlayer;

public record ServerPlayerRemovedReason(ServerPlayer player) implements IPlayerLifecycleEventReason, ITunnelRemoveReason {
}
