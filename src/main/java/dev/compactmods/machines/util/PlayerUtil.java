package dev.compactmods.machines.util;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.core.Capabilities;
import dev.compactmods.machines.core.LevelBlockPosition;
import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.machine.CompactMachineBlockEntity;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.room.RoomSize;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.api.room.IRoomHistory;
import dev.compactmods.machines.api.room.history.IRoomHistoryItem;
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
import net.minecraft.world.level.chunk.LevelChunk;
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

        ServerLevel compactWorld = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            throw new MissingDimensionException("Compact dimension not found; player attempted to enter machine.");
        }

        if(machineLevel.getBlockEntity(machinePos) instanceof CompactMachineBlockEntity tile) {
            final boolean grantAdvancement = !tile.mapped();

            ChunkPos targetRoom = tile.getInternalChunkPos().orElseThrow();
            if (player.level.dimension().equals(Registration.COMPACT_DIMENSION) && player.chunkPosition().equals(targetRoom)) {
                if (player instanceof ServerPlayer sp) {
                    AdvancementTriggers.RECURSIVE_ROOMS.trigger(sp);
                }

                return;
            }

            try {
                teleportPlayerIntoRoom(serv, player, targetRoom, grantAdvancement);

                // Mark the player as inside the machine, set external spawn, and yeet
                player.getCapability(Capabilities.ROOM_HISTORY).ifPresent(hist -> {
                    LevelBlockPosition pos = LevelBlockPosition.fromEntity(player);
                    hist.addHistory(new PlayerRoomHistoryItem(pos, tile.machineId));
                });
            } catch (NonexistentRoomException e) {
                CompactMachines.LOGGER.fatal("Critical error; could not enter a freshly-created room instance.", e);
            }
        }
    }

    private static void teleportPlayerIntoRoom(MinecraftServer serv, Player player, ChunkPos room, boolean grantAdvancement) throws MissingDimensionException, NonexistentRoomException {
        final var compactDim = serv.getLevel(Registration.COMPACT_DIMENSION);
        final var spawn = Rooms.getSpawn(serv, room);
        final var roomSize = Rooms.sizeOf(serv, room);

        if (spawn == null) {
            CompactMachines.LOGGER.error("Room %s could not load spawn info.".formatted(room));
            return;
        }

        serv.submitAsync(() -> {
            Vec3 sp = spawn.getExactPosition();
            Vec3 sr = spawn.getRotation().orElse(new Vec3(player.xRotO, player.yRotO, 0));

            if (player instanceof ServerPlayer servPlayer) {
                servPlayer.teleportTo(
                        compactDim,
                        sp.x,
                        sp.y,
                        sp.z,
                        (float) sr.y,
                        (float) sr.x);

                if (grantAdvancement)
                    AdvancementTriggers.getTriggerForMachineClaim(roomSize).trigger(servPlayer);
            }
        });
    }

    public static void teleportPlayerOutOfMachine(ServerLevel world, @Nonnull ServerPlayer serverPlayer) {

        MinecraftServer serv = world.getServer();

        final LazyOptional<IRoomHistory> history = serverPlayer.getCapability(Capabilities.ROOM_HISTORY);

        if (!history.isPresent()) {
            howDidYouGetThere(serverPlayer);
            return;
        }

        history.ifPresent(hist -> {
            ChunkPos currentRoomChunk = new ChunkPos(serverPlayer.blockPosition());

            if (hist.hasHistory()) {
                final IRoomHistoryItem prevArea = hist.pop();

                var spawnPoint = prevArea.getEntryLocation();

                final var level = spawnPoint.level(serv);

                Vec3 worldPos, entryRot;
                worldPos = spawnPoint.getExactPosition();
                entryRot = spawnPoint.getRotation().orElse(Vec3.ZERO);

                serverPlayer.teleportTo(level, worldPos.x(), worldPos.y(), worldPos.z(), (float) entryRot.y, (float) entryRot.x);
            } else {
                howDidYouGetThere(serverPlayer);

                hist.clear();
                teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
            }

            final LevelChunk chunk = serv.getLevel(Registration.COMPACT_DIMENSION)
                    .getChunk(currentRoomChunk.x, currentRoomChunk.z);

            // TODO - Send changed players packet to other clients
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
        Vec3 worldPos = LocationUtil.blockPosToVector(level.getSharedSpawnPos());

        if (player.getRespawnPosition() != null)
            worldPos = LocationUtil.blockPosToVector(player.getRespawnPosition());

        player.teleportTo(level, worldPos.x(), worldPos.y(), worldPos.z(), 0, player.getRespawnAngle());
    }
}
