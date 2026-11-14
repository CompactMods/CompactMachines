package dev.compactmods.machines.shrinking.capability;

import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.CompactDimensionTransitions;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.capability.RoomCapabilities;
import dev.compactmods.machines.shrinking.Shrinking;
import dev.compactmods.machines.shrinking.ShrinkingHelper;
import dev.compactmods.machines.shrinking.api.history.PlayerTeleportHistoryManager;
import dev.compactmods.machines.shrinking.api.capability.PlayerShrinkingHandler;
import dev.compactmods.machines.shrinking.api.history.RoomExitResult;
import dev.compactmods.machines.shrinking.api.history.PlayerRoomHistoryEntry;
import dev.compactmods.machines.shrinking.api.history.RoomEntryMethod;
import dev.compactmods.machines.shrinking.api.history.RoomEntryResult;
import dev.compactmods.machines.shrinking.network.SyncRoomMetadataPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Util;
import net.neoforged.neoforge.network.PacketDistributor;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class ServerPlayerShrinkingHandler implements PlayerShrinkingHandler {

    final MinecraftServer server;
    final ServerPlayer player;
    private final ServerLevel compactDim;
    private final RoomInstance roomInstance;
    private final PlayerTeleportHistoryManager historyManager;

    public ServerPlayerShrinkingHandler(ServerPlayer player, RoomInstance roomInstance) {
        this.server = player.level().getServer();
        this.historyManager = player.getCapability(Shrinking.HISTORY_MANAGER);
        this.roomInstance = roomInstance;
        this.player = player;
        this.compactDim = CompactDimension.forServer(server);
    }

    @Override
    public CompletableFuture<RoomEntryResult> tryEnter(RoomEntryMethod method) {
        final var result = historyManager.push(roomInstance, method);

        if (result == RoomEntryResult.FAILED_TOO_FAR_DOWN) {
            player.sendOverlayMessage(Component.translatableWithFallback("compactmachines.errors.too_far_down", "An otherworldly force prevents you from shrinking more.")
                    .withStyle(ChatFormatting.DARK_RED)
                    .withStyle(ChatFormatting.ITALIC));

            return CompletableFuture.completedFuture(result);
        }

        if (result.successful()) {
            // Mark current room
            player.setData(Shrinking.CURRENT_ROOM_CODE, roomInstance.code());

            return server.submit(() -> {
                final var spawnManager = roomInstance.getCapability(RoomCapabilities.SPAWN_MANAGER);

                player.getCooldowns().addCooldown(Shrinking.Items.PERSONAL_SHRINKING_DEVICE.getId(), 25);

                final var spawns = spawnManager.spawns();
                final var spawn = spawns.forPlayer(player.getUUID()).orElse(spawns.defaultSpawn());
                player.teleport(CompactDimensionTransitions.to(compactDim, spawn.position(), spawn.rotation()));

                PacketDistributor.sendToPlayer(player, new SyncRoomMetadataPacket(roomInstance.code(), Util.NIL_UUID));

                return result;
            });
        }

        return CompletableFuture.completedFuture(result);
    }

    @Override
    public CompletableFuture<RoomExitResult> tryExit() {
        if (!CompactDimension.isLevelCompact(player.level()))
            return CompletableFuture.completedFuture(RoomExitResult.FAILED_NOT_IN_COMPACT_DIM);

        final var registry = server.getCapability(RoomCapabilities.REGISTRY);
        final CompletableFuture<RoomExitResult> leaveResult = server.submit(() -> historyManager
                .pop(1)
                .map(entry -> {
                    player.getCooldowns().addCooldown(Shrinking.Items.PERSONAL_SHRINKING_DEVICE.getId(), 25);

                    final var roomInstance = registry.get(entry.roomCode()).orElseThrow();
                    return entry.entryPoint().exit(server, player, roomInstance);
                }).orElseGet(() -> {
                    ShrinkingHelper.teleportPlayerToRespawnOrOverworld(server, player);

                    return RoomExitResult.SUCCESS_WENT_TO_SPAWN;
                }));

        // Listener for post-leave; if successful then pop a history entry from the main history manager
        return leaveResult.thenApply(res -> {
            final var previousEntry = historyManager.peek();
            previousEntry.ifPresentOrElse(prev -> {
                player.setData(Shrinking.CURRENT_ROOM_CODE, prev.roomCode());
            }, () -> {
                player.removeData(Shrinking.CURRENT_ROOM_CODE);
            });

            return res;
        });
    }
}
