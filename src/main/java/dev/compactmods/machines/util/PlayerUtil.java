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

    public static void teleportPlayerIntoMachine(Level level, Player player, BlockPos machinePos, RoomSize size) {
        MinecraftServer serv = level.getServer();

        ServerLevel compactWorld = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.warn("Compact dimension not found; player attempted to enter machine.");
            return;
        }

        CompactMachineBlockEntity tile = (CompactMachineBlockEntity) level.getBlockEntity(machinePos);
        if (tile == null)
            return;

        final boolean grantAdvancement = !tile.mapped();
        if (!tile.mapped()) {
            ChunkPos newRoomPos;
            try {
                newRoomPos = Rooms.createNew(serv, size, player.getUUID());
                Machines.createAndLink(serv, level, machinePos, tile, newRoomPos);
            } catch (MissingDimensionException e) {
                CompactMachines.LOGGER.error("Error occurred while generating new room and machine info for first player entry.", e);
                return;
            }
        }

        serv.submitAsync(() -> {
            LevelBlockPosition spawn = tile.getSpawn().orElse(null);
            if (spawn == null) {
                CompactMachines.LOGGER.error("Machine " + tile.machineId + " could not load spawn info.");
                return;
            }

            try {
                // Mark the player as inside the machine, set external spawn, and yeet
                addPlayerToMachine(player, machinePos);
            } catch (Exception ex) {
                CompactMachines.LOGGER.error(ex);
            }

            Vec3 sp = spawn.getExactPosition();
            Vec3 sr = spawn.getRotation().orElse(new Vec3(player.xRotO, player.yRotO, 0));

            if (player instanceof ServerPlayer servPlayer) {
                servPlayer.teleportTo(
                        compactWorld,
                        sp.x,
                        sp.y,
                        sp.z,
                        (float) sr.y,
                        (float) sr.x);

                if (grantAdvancement)
                    AdvancementTriggers.getTriggerForMachineClaim(size).trigger(servPlayer);
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

    public static void addPlayerToMachine(Player player, BlockPos machinePos) {
        MinecraftServer serv = player.getServer();
        if (serv == null)
            return;

        CompactMachineBlockEntity tile = (CompactMachineBlockEntity) player.getLevel().getBlockEntity(machinePos);
        if (tile == null)
            return;

        tile.getInternalChunkPos().ifPresent(mChunk -> {
            final LevelChunk chunk = serv.getLevel(Registration.COMPACT_DIMENSION)
                    .getChunk(mChunk.x, mChunk.z);

            player.getCapability(Capabilities.ROOM_HISTORY)
                    .ifPresent(hist -> {
                        LevelBlockPosition pos = LevelBlockPosition.fromEntity(player);
                        hist.addHistory(new PlayerRoomHistoryItem(pos, tile.machineId));
                    });

            // TODO - player tracking packet
        });
    }

}
