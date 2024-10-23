package dev.compactmods.machines.util;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
import dev.compactmods.machines.dimension.MissingDimensionException;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.location.PreciseDimensionalPosition;
import dev.compactmods.machines.location.SimpleTeleporter;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.room.RoomCapabilities;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import dev.compactmods.machines.room.history.PlayerRoomHistoryItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public abstract class PlayerUtil {
    public static Optional<GameProfile> getProfileByUUID(LevelAccessor world, UUID uuid) {
        Player player = world.getPlayerByUUID(uuid);
        if (player == null)
            return Optional.empty();

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }

    public static void teleportPlayerIntoMachine(Level machineLevel, Player player, BlockPos machinePos) throws MissingDimensionException {
        MinecraftServer serv = machineLevel.getServer();

        ServerLevel compactWorld = serv.getLevel(CompactDimension.LEVEL_KEY);
        if (compactWorld == null) {
            throw new MissingDimensionException("Compact dimension not found; player attempted to enter machine.");
        }

        if (machineLevel.getBlockEntity(machinePos) instanceof CompactMachineBlockEntity tile) {
            final var targetRoom = tile.getConnectedRoom();
            boolean grantAdvancement = targetRoom.isEmpty();

            targetRoom.ifPresent(room -> {
                if (player.level().dimension().equals(CompactDimension.LEVEL_KEY) && player.chunkPosition().equals(room)) {
                    if (player instanceof ServerPlayer sp) {
                        AdvancementTriggers.RECURSIVE_ROOMS.trigger(sp);
                    }

                    return;
                }

                try {
                    final var entry = PreciseDimensionalPosition.fromPlayer(player);

                    teleportPlayerIntoRoom(serv, player, room, grantAdvancement);

                    // Mark the player as inside the machine, set external spawn, and yeet
                    player.getCapability(RoomCapabilities.ROOM_HISTORY).ifPresent(hist -> {
                        hist.addHistory(new PlayerRoomHistoryItem(entry, tile.getLevelPosition()));
                    });
                } catch (MissingDimensionException | NonexistentRoomException e) {
                    CompactMachines.LOGGER.fatal("Critical error; could not enter a freshly-created room instance.", e);
                }
            });
        }
    }

    public static void teleportPlayerIntoRoom(MinecraftServer serv, Player player, ChunkPos room, boolean grantAdvancement) throws MissingDimensionException, NonexistentRoomException {
        final var compactDim = serv.getLevel(CompactDimension.LEVEL_KEY);
        final var spawn = Rooms.getSpawn(serv, room);
        final var roomSize = Rooms.sizeOf(serv, room);

        if (spawn == null) {
            CompactMachines.LOGGER.error("Room %s could not load spawn info.".formatted(room));
            return;
        }

        serv.submitAsync(() -> {
            Vec3 sp = spawn.getExactPosition();
            Vec2 sr = spawn.getRotation().orElse(player.getRotationVector());

            if (player instanceof ServerPlayer servPlayer) {
                servPlayer.changeDimension(compactDim, SimpleTeleporter.to(sp, sr));
                servPlayer.setCamera(null); // force camera update
                if (grantAdvancement)
                    AdvancementTriggers.getTriggerForMachineClaim(roomSize).trigger(servPlayer);
            }
        });
    }

    public static void teleportPlayerOutOfMachine(ServerLevel world, @Nonnull ServerPlayer serverPlayer) {

        MinecraftServer serv = world.getServer();

        final LazyOptional<IRoomHistory> history = serverPlayer.getCapability(RoomCapabilities.ROOM_HISTORY);

        if (!history.isPresent()) {
            howDidYouGetThere(serverPlayer);
            return;
        }

        history.ifPresent(hist -> {
            if (hist.hasHistory()) {
                final IRoomHistoryItem prevArea = hist.pop();

                var spawnPoint = prevArea.getEntryLocation();

                final var level = spawnPoint.level(serv);

                Vec3 worldPos = spawnPoint.getExactPosition();
                Vec2 entryRot = spawnPoint.getRotation().orElse(Vec2.ZERO);

                serverPlayer.changeDimension(level, SimpleTeleporter.to(worldPos, entryRot));
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
}
