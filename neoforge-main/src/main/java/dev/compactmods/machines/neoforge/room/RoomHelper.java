package dev.compactmods.machines.neoforge.room;

import dev.compactmods.machines.api.room.RoomApi;
import dev.compactmods.machines.api.room.RoomInstance;
import dev.compactmods.machines.api.room.history.RoomEntryPoint;
import dev.compactmods.machines.LoggingUtil;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.neoforge.dimension.SimpleTeleporter;
import dev.compactmods.machines.neoforge.shrinking.Shrinking;
import dev.compactmods.machines.neoforge.util.ForgePlayerUtil;
import dev.compactmods.machines.player.PlayerEntryPointHistory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;

public abstract class RoomHelper {

    private static final Logger LOGS = LoggingUtil.modLog();

    public static boolean entityInsideRoom(LivingEntity entity, String roomCode) {
        // Recursion check. Player is inside the room being queried.
        if (entity.level().dimension().equals(CompactDimension.LEVEL_KEY)) {
            return RoomApi.chunks(roomCode).hasChunk(entity.chunkPosition());
        }

        return false;
    }

    public static void teleportPlayerIntoMachine(Level machineLevel, ServerPlayer player, GlobalPos machinePos, String roomCode) {
        MinecraftServer serv = machineLevel.getServer();

        LOGS.debug("Player {} entering machine at: {}", player.getName(), machinePos);
        RoomApi.room(roomCode).ifPresent(roomInfo -> {
            try {
                teleportPlayerIntoRoom(serv, player, roomInfo, RoomEntryPoint.playerEnteringMachine(player));
            } catch (MissingDimensionException e) {
                LOGS.fatal("Critical error; could not enter a freshly-created room instance.", e);
            }
        });
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, ServerPlayer player, RoomInstance room, RoomEntryPoint entryPoint)
            throws MissingDimensionException {
        final var compactDim = CompactDimension.forServer(serv);

        final var history = PlayerEntryPointHistory.forServer(serv);
        final var result = history.enterRoom(player, room.code(), entryPoint);

        LOGS.debug("Entry result: {}", result);

        switch (result) {
            case FAILED_TOO_FAR_DOWN -> {
                player.displayClientMessage(Component.translatableWithFallback("compactmachines.errors.too_far_down", "An otherworldly force prevents you from shrinking more.")
                        .withStyle(ChatFormatting.DARK_RED)
                        .withStyle(ChatFormatting.ITALIC), true);
            }

            case SUCCESS -> {

                serv.submitAsync(() -> {
                    player.getCooldowns().addCooldown(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), 25);

                    final var spawns = RoomApi.spawnManager(room.code()).spawns();
                    final var spawn = spawns.forPlayer(player.getUUID()).orElse(spawns.defaultSpawn());
                    player.changeDimension(compactDim, SimpleTeleporter.to(spawn.position(), spawn.rotation()));
                });

                // Mark current room
                player.setData(Rooms.LAST_ROOM_ENTRYPOINT, RoomEntryPoint.playerEnteringMachine(player));
            }
        }
    }

    public static void teleportPlayerOutOfRoom(@Nonnull ServerPlayer serverPlayer) {
        if (!serverPlayer.level().dimension().equals(CompactDimension.LEVEL_KEY))
            return;

        MinecraftServer serv = serverPlayer.getServer();
        assert serv != null;

        final PlayerEntryPointHistory history;
        try {
            history = PlayerEntryPointHistory.forServer(serv);
        } catch (MissingDimensionException e) {
            LOGS.error("Missing compact dimension data. Teleporting player {} to their default spawn...", serverPlayer.getUUID());
            ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
            return;
        }

        serv.submit(() -> {
            history.lastHistory(serverPlayer).ifPresentOrElse(
                    before -> {
                        serverPlayer.getCooldowns().addCooldown(Shrinking.PERSONAL_SHRINKING_DEVICE.get(), 25);

                        serverPlayer.setData(Rooms.LAST_ROOM_ENTRYPOINT, before.entryPoint());
                        history.popHistory(serverPlayer, 1);

                        final var location = before.entryPoint().entryLocation();
                        final var level = serv.getLevel(location.dimension());
                        if (level != null) {
                            LOGS.debug("Teleporting player {} to {} as they jump up a level...", serverPlayer.getUUID(), location);
                            serverPlayer.changeDimension(level, SimpleTeleporter.to(location.position(), location.rotation()));
                        } else {
                            LOGS.error("Player tracking points to an unknown dimension. Teleporting player {} to their default spawn instead.", serverPlayer.getUUID());
                            ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
                        }
                    },
                    () -> {
                        serverPlayer.removeData(Rooms.LAST_ROOM_ENTRYPOINT);
                        ForgePlayerUtil.teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
                    }
            );

        });
    }
}
