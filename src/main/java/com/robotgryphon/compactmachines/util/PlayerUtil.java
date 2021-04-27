package com.robotgryphon.compactmachines.util;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.api.core.Messages;
import com.robotgryphon.compactmachines.block.tiles.CompactMachineTile;
import com.robotgryphon.compactmachines.config.ServerConfig;
import com.robotgryphon.compactmachines.core.Registration;
import com.robotgryphon.compactmachines.data.machine.CompactMachineInternalData;
import com.robotgryphon.compactmachines.data.player.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.data.world.ExternalMachineData;
import com.robotgryphon.compactmachines.data.world.InternalMachineData;
import com.robotgryphon.compactmachines.network.MachinePlayersChangedPacket;
import com.robotgryphon.compactmachines.network.NetworkHandler;
import com.robotgryphon.compactmachines.reference.EnumMachineSize;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

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
        if (serverWorld.dimension() == Registration.COMPACT_DIMENSION) {
            IFormattableTextComponent msg = TranslationUtil
                    .message(Messages.CANNOT_ENTER_MACHINE)
                    .withStyle(TextFormatting.RED);

            serverPlayer.displayClientMessage(msg, true);
            return;
        }

        ServerWorld compactWorld = serv.getLevel(Registration.COMPACT_DIMENSION);
        if (compactWorld == null) {
            CompactMachines.LOGGER.warn("Compact dimension not found; player attempted to enter machine.");
            return;
        }

        CompactMachineTile tile = (CompactMachineTile) serverWorld.getBlockEntity(machinePos);
        if (tile == null)
            return;

        if (!tile.mapped()) {
            ExternalMachineData extern = ExternalMachineData.get(serv);
            InternalMachineData intern = InternalMachineData.get(serv);

            int nextId = intern.getNextId();
            Vector3i location = MathUtil.getRegionPositionByIndex(nextId);

            int centerY = ServerConfig.MACHINE_FLOOR_Y.get() + (size.getInternalSize() / 2);
            BlockPos newCenter = new BlockPos(
                    (location.getX() * 1024) + 8,
                    centerY,
                    (location.getZ() * 1024) + 8);

            // Generate a new machine inside and update the tile
            CompactStructureGenerator.generateCompactStructure(compactWorld, size, newCenter);
            ChunkPos machineChunk = new ChunkPos(newCenter);
            tile.setMachineId(nextId);
            extern.machineMapping.put(nextId, machineChunk);
            extern.machineLocations.put(nextId, new DimensionalPosition(serverWorld.dimension(), machinePos));
            extern.setDirty();

            BlockPos.Mutable newSpawn = newCenter.mutable();
            newSpawn.setY(newSpawn.getY() - (size.getInternalSize() / 2));

            try {
                intern.register(machineChunk, new CompactMachineInternalData(
                        serverPlayer.getUUID(),
                        newCenter,
                        newSpawn,
                        size
                ));
            } catch (OperationNotSupportedException e) {
                CompactMachines.LOGGER.warn(e);
            }
        }

        serv.submitAsync(() -> {
            tile.getInternalData().ifPresent(mach -> {
                BlockPos spawn = mach.getSpawn();
                InternalMachineData.get(serv).setDirty();
                try {
                    // Mark the player as inside the machine, set external spawn, and yeet
                    addPlayerToMachine(serverPlayer, machinePos);
                } catch (Exception ex) {
                    CompactMachines.LOGGER.error(ex);
                }

                serverPlayer.teleportTo(
                        compactWorld,
                        spawn.getX() + 0.5,
                        spawn.getY(),
                        spawn.getZ() + 0.5,
                        serverPlayer.yRot,
                        serverPlayer.xRot);
            });

            // TODO - Move machine generation to new method
//                int nextID = serverData.getNextMachineId();
//
//                BlockPos center = getCenterForNewMachine(nextID, size);
//
//                CompactStructureGenerator.generateCompactStructure(compactWorld, size, center);
//
//                tile.setMachineId(nextID);
//                CompactMachineRegistrationData regData = new CompactMachineRegistrationData(nextID, center, serverPlayer.getUUID(), size);
//                regData.setWorldPosition(serverWorld, machinePos);
//
//                serverData.registerMachine(nextID, regData);
//                machineData.setDirty();
//
//                BlockPos.Mutable spawn = center.mutable();
//                spawn.setY(ServerConfig.MACHINE_FLOOR_Y.get());
//
//                spawnPoint = spawn.immutable();
        });
    }

    public static void teleportPlayerOutOfMachine(ServerWorld world, ServerPlayerEntity serverPlayer) {

        MinecraftServer serv = world.getServer();
        CompactMachinePlayerData playerData = CompactMachinePlayerData.get(serv);
        if (playerData == null)
            return;

        Optional<DimensionalPosition> externalSpawn = playerData.getExternalSpawn(serverPlayer);
        if (!externalSpawn.isPresent()) {
            serverPlayer.displayClientMessage(
                    new TranslationTextComponent("messages.%s.how_did_you_get_here", CompactMachines.MOD_ID),
                    true
            );

            return;
        }

        ChunkPos currentMachine = new ChunkPos(serverPlayer.blockPosition());

        DimensionalPosition spawnPoint = externalSpawn.get();

        Optional<ServerWorld> outsideWorld = spawnPoint.getWorld(world.getServer());
        outsideWorld.ifPresent(w -> {
            Vector3d worldPos = spawnPoint.getPosition();
            serverPlayer.teleportTo(w, worldPos.x(), worldPos.y(), worldPos.z(), serverPlayer.yRot, serverPlayer.xRot);

            // CompactMachinePlayerUtil.removePlayerFromMachine(serverPlayer, MACHINE_POS);
            // TODO - Networking for player leaving machine
//                machine.ifPresent(m -> {
//                    CompactMachinePlayerUtil.removePlayerFromMachine(serverPlayer,
//                            machineInfo.getOutsidePosition(serverPlayer.getServer()).getBlockPosition(),
//                            m.getId());
//                });
        });

        // TODO
//        Optional<CompactMachinePlayerData> machinePlayers = serverData.getPlayerData(machineInfo.getId());
//        if (!machinePlayers.isPresent()) {
//            // No player data for machine, wut
//            CompactMachines.LOGGER.warn("Warning: Machine player data not set but machine registered, and player is inside. Machine ID: {}", machineInfo.getId());
//            serverPlayer.displayClientMessage(new TranslationTextComponent("ah_crap"), true);
//            return;
//        }
//
//        Optional<DimensionalPosition> lastPos = machinePlayers.get().getExternalSpawn(serverPlayer);
//        if (!lastPos.isPresent()) {
//            // PANIC
//
//            return;
//        } else {
//            DimensionalPosition p = lastPos.get();
//            Vector3d bp = p.getPosition();
//            Optional<ServerWorld> outsideWorld = p.getWorld(world.getServer());
//            outsideWorld.ifPresent(w -> {
//                machine.ifPresent(m -> {
//                    serverPlayer.teleportTo(w, bp.x(), bp.y(), bp.z(), serverPlayer.yRot, serverPlayer.xRot);
//                    CompactMachinePlayerUtil.removePlayerFromMachine(serverPlayer,
//                            machineInfo.getOutsidePosition(serverPlayer.getServer()).getBlockPosition(),
//                            m.getId());
//                });
//            });
//        }

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

            playerData.addPlayer(serverPlayer, mChunk);
            playerData.setDirty();

            MachinePlayersChangedPacket p = MachinePlayersChangedPacket.Builder.create(serv)
                    .forMachine(mChunk)
                    .forPlayer(serverPlayer)
                    .enteredFrom(tile.machineId)
                    .build();

            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> serverPlayer.getLevel().getChunkAt(machinePos)),
                    p);
        });
    }

    public static void removePlayerFromMachine(ServerPlayerEntity serverPlayer, BlockPos machinePos) {
        MinecraftServer serv = serverPlayer.getServer();

        CompactMachinePlayerData playerData = CompactMachinePlayerData.get(serv);
        if (playerData == null)
            return;

        playerData.removePlayer(serverPlayer);

        CompactMachineTile tile = (CompactMachineTile) serverPlayer.getLevel().getBlockEntity(machinePos);
        if (tile == null)
            return;

        tile.getInternalChunkPos().ifPresent(mChunk -> {

            playerData.removePlayer(serverPlayer);
            playerData.setDirty();

            MachinePlayersChangedPacket p = MachinePlayersChangedPacket.Builder.create(serv)
                    .forMachine(mChunk)
                    .forPlayer(serverPlayer)
                    .build();

            NetworkHandler.MAIN_CHANNEL.send(
                    PacketDistributor.TRACKING_CHUNK.with(() -> serverPlayer.getLevel().getChunkAt(machinePos)),
                    p);
        });
    }
}
