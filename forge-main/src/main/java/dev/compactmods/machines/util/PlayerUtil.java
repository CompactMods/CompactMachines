package dev.compactmods.machines.util;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.location.PreciseDimensionalPosition;
import dev.compactmods.machines.location.SimpleTeleporter;
import dev.compactmods.machines.room.RoomCapabilities;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import dev.compactmods.machines.room.history.PlayerRoomHistoryItem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public abstract class PlayerUtil {
    public static Optional<GameProfile> getProfileByUUID(MinecraftServer server, UUID uuid) {
        final var player = server.getPlayerList().getPlayer(uuid);
        if (player == null) {
            var profile = new GameProfile(uuid, "Unknown");
            var p2 = server.getSessionService().fillProfileProperties(profile, false);
            return Optional.ofNullable(p2);
        }

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }

    public static Optional<GameProfile> getProfileByUUID(LevelAccessor world, UUID uuid) {
        final var player = world.getPlayerByUUID(uuid);
        if (player == null)
            return Optional.empty();

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }

    public static void teleportPlayerIntoMachine(Level machineLevel, Player player, LevelBlockPosition machinePos, IRoomRegistration room) throws MissingDimensionException {
        MinecraftServer serv = machineLevel.getServer();

        // Recursion check. Player tried to enter the room they're already in.
        if (player.level.dimension().equals(CompactDimension.LEVEL_KEY)) {
            final boolean recursion = player.getCapability(RoomCapabilities.ROOM_HISTORY).map(hist -> {
                if (player instanceof ServerPlayer sp && room.chunks().anyMatch(chunk -> sp.chunkPosition().equals(chunk))) {
                    AdvancementTriggers.RECURSIVE_ROOMS.trigger(sp);
                    return true;
                }

                return false;
            }).orElse(false);

            if (recursion) return;
        }

        try {
            final var entry = PreciseDimensionalPosition.fromPlayer(player);

            teleportPlayerIntoRoom(serv, player, room);

            // Mark the player as inside the machine, set external spawn, and yeet
            player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
                hist.addHistory(new PlayerRoomHistoryItem(entry, machinePos));
            });
        } catch (MissingDimensionException | NonexistentRoomException e) {
            CompactMachines.LOGGER.fatal("Critical error; could not enter a freshly-created room instance.", e);
        }
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, Player player, IRoomRegistration room)
            throws MissingDimensionException, NonexistentRoomException {
        final var compactDim = CompactDimension.forServer(serv);
        final var spawnLookup = CompactRoomProvider.instance(compactDim);

        serv.submitAsync(() -> {

            if (player instanceof ServerPlayer servPlayer) {
                servPlayer.changeDimension(compactDim, SimpleTeleporter.to(room.spawnPosition(spawnLookup), room.spawnRotation(spawnLookup)));
            }
        });
    }

    public static void teleportPlayerOutOfMachine(ServerLevel compactDim, @Nonnull ServerPlayer serverPlayer) {

        MinecraftServer serv = compactDim.getServer();

        final LazyOptional<IRoomHistory> history = serverPlayer.getCapability(RoomCapabilities.ROOM_HISTORY);

        if (!history.isPresent()) {
            howDidYouGetThere(serverPlayer);
            return;
        }

        history.ifPresent(hist -> {
            if (hist.hasHistory()) {
                final IRoomHistoryItem prevArea = hist.pop();

                var spawnPoint = prevArea.getEntryLocation();
                final var enteredMachine = prevArea.getMachine().getBlockPosition();

                final var level = spawnPoint.level(serv);
                serverPlayer.changeDimension(level, SimpleTeleporter.lookingAt(spawnPoint.position(), enteredMachine));
            } else {
                howDidYouGetThere(serverPlayer);

                hist.clear();
                teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
            }
        });
    }

    public static void howDidYouGetThere(@Nonnull ServerPlayer serverPlayer) {
        AdvancementTriggers.HOW_DID_YOU_GET_HERE.trigger(serverPlayer);

        serverPlayer.displayClientMessage(
                TranslationUtil.message(Messages.HOW_DID_YOU_GET_HERE),
                true
        );
    }

    public static void teleportPlayerToRespawnOrOverworld(MinecraftServer serv, @Nonnull ServerPlayer player) {
        ServerLevel level = Optional.ofNullable(serv.getLevel(player.getRespawnDimension())).orElse(serv.overworld());
        Vec3 worldPos = Vec3.atCenterOf(level.getSharedSpawnPos());

        if (player.getRespawnPosition() != null)
            worldPos = Vec3.atCenterOf(player.getRespawnPosition());

        player.changeDimension(level, SimpleTeleporter.to(worldPos));
    }

    public static Vec2 getLookDirection(Player player) {
        return new Vec2(player.xRotO, player.yRotO);
    }
}
