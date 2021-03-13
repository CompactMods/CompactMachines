package com.robotgryphon.compactmachines.util;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.data.CompactMachineServerData;
import com.robotgryphon.compactmachines.data.SavedMachineData;
import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.data.machines.CompactMachineRegistrationData;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
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

        SavedMachineData machineData = SavedMachineData.getInstance(world.getServer());
        CompactMachineServerData serverData = machineData.getData();

        Optional<CompactMachineRegistrationData> machine = serverData.getMachineContainingPosition(serverPlayer.position());

        if (!machine.isPresent()) {
            serverPlayer.displayClientMessage(
                    new TranslationTextComponent("not_inside_machine"),
                    true);

            return;
        }

        CompactMachineRegistrationData machineInfo = machine.get();

        Optional<CompactMachinePlayerData> machinePlayers = serverData.getPlayerData(machineInfo.getId());
        if (!machinePlayers.isPresent()) {
            // No player data for machine, wut
            CompactMachines.LOGGER.warn("Warning: Machine player data not set but machine registered, and player is inside. Machine ID: {}", machineInfo.getId());
            serverPlayer.displayClientMessage(new TranslationTextComponent("ah_crap"), true);
            return;
        }

        Optional<DimensionalPosition> lastPos = machinePlayers.get().getExternalSpawn(serverPlayer);
        if (!lastPos.isPresent()) {
            // PANIC

            return;
        } else {
            DimensionalPosition p = lastPos.get();
            Vector3d bp = p.getPosition();
            Optional<ServerWorld> outsideWorld = p.getWorld(world.getServer());
            outsideWorld.ifPresent(w -> {
                machine.ifPresent(m -> {
                    serverPlayer.teleportTo(w, bp.x(), bp.y(), bp.z(), serverPlayer.yRot, serverPlayer.xRot);
                    CompactMachinePlayerUtil.removePlayerFromMachine(serverPlayer,
                            machineInfo.getOutsidePosition(serverPlayer.getServer()).getBlockPosition(),
                            m.getId());
                });
            });
        }
    }
}
