package dev.compactmods.machines.util;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.block.tiles.CompactMachineTile;
import dev.compactmods.machines.config.ServerConfig;
import dev.compactmods.machines.core.Registration;
import dev.compactmods.machines.data.persistent.MachineConnections;
import dev.compactmods.machines.data.player.CompactMachinePlayerData;
import dev.compactmods.machines.data.persistent.CompactMachineData;
import dev.compactmods.machines.data.persistent.CompactRoomData;
import dev.compactmods.machines.network.CMPacketTargets;
import dev.compactmods.machines.network.MachinePlayersChangedPacket;
import dev.compactmods.machines.network.NetworkHandler;
import dev.compactmods.machines.reference.EnumMachineSize;
import dev.compactmods.machines.rooms.IRoomHistoryItem;
import dev.compactmods.machines.rooms.capability.CapabilityRoomHistory;
import dev.compactmods.machines.rooms.capability.IRoomHistory;
import dev.compactmods.machines.teleportation.DimensionalPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.naming.OperationNotSupportedException;
import java.util.Optional;
import java.util.UUID;

public abstract class PlayerUtil {
    public static Optional<GameProfile> getProfileByUUID(IWorld world, UUID uuid) {
        PlayerEntity player = world.getPlayerByUUID(uuid);
        if (player == null)
            return Optional.empty();

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }

    public static DimensionalPosition getPlayerDimensionalPosition(PlayerEntity player) {
        Vector3d pos = player.position();
        RegistryKey<World> dim = player.level.dimension();

        return new DimensionalPosition(dim, pos);
    }

    public static void teleportPlayerIntoMachine(ServerPlayerEntity serverPlayer, BlockPos machinePos, EnumMachineSize size) {
        ServerWorld serverWorld = serverPlayer.getLevel();

        MinecraftServer serv = serverWorld.getServer();

        ServerWorld compactWorld = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.warn("Compact dimension not found; player attempted to enter machine.");
            return;
        }

        CompactMachineTile tile = (CompactMachineTile) serverWorld.getBlockEntity(machinePos);
        if (tile == null)
            return;

        if (!tile.mapped()) {
            CompactMachineData machines = CompactMachineData.get(serv);
            CompactRoomData rooms = CompactRoomData.get(serv);
            MachineConnections connections = MachineConnections.get(serv);

            if (machines == null || rooms == null || connections == null) {
                CompactMachines.LOGGER.error("Could not load world saved data while creating new machine and room.");
                return;
            }

            int nextId = rooms.getNextId();
            Vector3i location = MathUtil.getRegionPositionByIndex(nextId);

            int centerY = ServerConfig.MACHINE_FLOOR_Y.get() + (size.getInternalSize() / 2);
            BlockPos newCenter = MathUtil.getCenterWithY(location, centerY);

            // Generate a new machine inside and update the tile
            CompactStructureGenerator.generateCompactStructure(compactWorld, size, newCenter);
            ChunkPos machineChunk = new ChunkPos(newCenter);
            tile.setMachineId(nextId);

            connections.graph.addMachine(nextId);
            connections.graph.addRoom(machineChunk);
            connections.graph.connectMachineToRoom(nextId, machineChunk);
            connections.setDirty();

            machines.setMachineLocation(nextId, new DimensionalPosition(serverWorld.dimension(), machinePos));

            try {
                rooms.createNew()
                        .owner(serverPlayer.getUUID())
                        .size(size)
                        .chunk(machineChunk)
                        .register();
            } catch (OperationNotSupportedException e) {
                CompactMachines.LOGGER.warn(e);
            }
        }

        serv.submitAsync(() -> {
            DimensionalPosition spawn = tile.getSpawn().orElse(null);
            if (spawn == null) {
                CompactMachines.LOGGER.error("Machine " + tile.machineId + " could not load spawn info.");
                return;
            }

            try {
                // Mark the player as inside the machine, set external spawn, and yeet
                addPlayerToMachine(serverPlayer, machinePos);
            } catch (Exception ex) {
                CompactMachines.LOGGER.error(ex);
            }

            Vector3d sp = spawn.getPosition();
            Vector3d sr = spawn.getRotation() != Vector3d.ZERO ?
                    spawn.getRotation() : new Vector3d(serverPlayer.xRot, serverPlayer.yRot, 0);

            serverPlayer.teleportTo(
                    compactWorld,
                    sp.x,
                    sp.y,
                    sp.z,
                    (float) sr.y,
                    (float) sr.x);
        });

        // TODO - Move machine generation to new method
    }

    // TODO - Overhaul this to handle nested machines
    public static void teleportPlayerOutOfMachine(ServerWorld world, @Nonnull ServerPlayerEntity serverPlayer) {

        MinecraftServer serv = world.getServer();
        CompactMachinePlayerData playerData = CompactMachinePlayerData.get(serv);
        if (playerData == null)
            return;

        final LazyOptional<IRoomHistory> history = serverPlayer.getCapability(CapabilityRoomHistory.HISTORY_CAPABILITY);

        if (!history.isPresent()) {
            howDidYouGetThere(serverPlayer);
            return;
        }

        history.ifPresent(hist -> {
            ChunkPos currentRoomChunk = new ChunkPos(serverPlayer.blockPosition());

            if(hist.hasHistory()) {
                final IRoomHistoryItem prevArea = hist.pop();

                DimensionalPosition spawnPoint = prevArea.getEntryLocation();

                ServerWorld w = spawnPoint.getWorld(serv).orElse(serv.overworld());
                Vector3d worldPos, entryRot;

                if (serv.getLevel(spawnPoint.getDimension()) != null) {
                    worldPos = spawnPoint.getPosition();
                    entryRot = spawnPoint.getRotation();

                    serverPlayer.teleportTo(w, worldPos.x(), worldPos.y(), worldPos.z(), (float) entryRot.y, (float) entryRot.x);
                } else {
                    teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
                }
            } else {
                howDidYouGetThere(serverPlayer);

                teleportPlayerToRespawnOrOverworld(serv, serverPlayer);
            }

            final Chunk chunk = serv.getLevel(Registration.COMPACT_DIMENSION)
                    .getChunk(currentRoomChunk.x, currentRoomChunk.z);

            MachinePlayersChangedPacket p = MachinePlayersChangedPacket.Builder.create(serv)
                    .forMachine(currentRoomChunk)
                    .forPlayer(serverPlayer)
                    .build();

            NetworkHandler.MAIN_CHANNEL.send(CMPacketTargets.TRACKING_ROOM.with(() -> chunk), p);
        });
    }

    private static void howDidYouGetThere(@Nonnull ServerPlayerEntity serverPlayer) {
        AdvancementTriggers.HOW_DID_YOU_GET_HERE.trigger(serverPlayer);

        serverPlayer.displayClientMessage(
                TranslationUtil.message(Messages.HOW_DID_YOU_GET_HERE),
                true
        );
    }

    private static void teleportPlayerToRespawnOrOverworld(MinecraftServer serv, @Nonnull ServerPlayerEntity player) {
        ServerWorld level = Optional.ofNullable(serv.getLevel(player.getRespawnDimension())).orElse(serv.overworld());
        Vector3d worldPos = LocationUtil.blockPosToVector(level.getSharedSpawnPos());

        if (player.getRespawnPosition() != null)
            worldPos = LocationUtil.blockPosToVector(player.getRespawnPosition());

        player.teleportTo(level, worldPos.x(), worldPos.y(), worldPos.z(), 0, player.getRespawnAngle());
    }

    public static void addPlayerToMachine(ServerPlayerEntity serverPlayer, BlockPos machinePos) {
        MinecraftServer serv = serverPlayer.getServer();
        if (serv == null)
            return;

        CompactMachinePlayerData playerData = CompactMachinePlayerData.get(serv);
        if (playerData == null)
            return;

        CompactMachineTile tile = (CompactMachineTile) serverPlayer.getLevel().getBlockEntity(machinePos);
        if (tile == null)
            return;

        tile.getInternalChunkPos().ifPresent(mChunk -> {
            final Chunk chunk = serv.getLevel(Registration.COMPACT_DIMENSION)
                    .getChunk(mChunk.x, mChunk.z);

            // TODO - Add player to machine data
            // playerData.addPlayer(serverPlayer, mChunk);
            playerData.setDirty();

            MachinePlayersChangedPacket p = MachinePlayersChangedPacket.Builder.create(serv)
                    .forMachine(mChunk)
                    .forPlayer(serverPlayer)
                    .enteredFrom(tile.machineId)
                    .build();

            NetworkHandler.MAIN_CHANNEL.send(CMPacketTargets.TRACKING_ROOM.with(() -> chunk), p);
        });
    }

}
