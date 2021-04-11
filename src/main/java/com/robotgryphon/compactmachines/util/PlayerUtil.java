package com.robotgryphon.compactmachines.util;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.data.legacy.CompactMachineServerData;
import com.robotgryphon.compactmachines.data.legacy.SavedMachineData;
import com.robotgryphon.compactmachines.data.player.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.data.legacy.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.data.world.ExternalMachineData;
import com.robotgryphon.compactmachines.data.world.InternalMachineData;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
}
