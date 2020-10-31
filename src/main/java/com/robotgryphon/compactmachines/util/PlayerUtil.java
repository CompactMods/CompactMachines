package com.robotgryphon.compactmachines.util;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.CompactMachines;
import com.robotgryphon.compactmachines.data.CompactMachineMemoryData;
import com.robotgryphon.compactmachines.data.machines.CompactMachineData;
import com.robotgryphon.compactmachines.data.machines.CompactMachinePlayerData;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;
import java.util.UUID;

public abstract class PlayerUtil {
    public static Optional<GameProfile> getProfileByUUID(IWorld world, UUID uuid) {
        PlayerEntity player = world.getPlayerByUuid(uuid);
        if(player == null)
            return Optional.empty();

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }

    public static DimensionalPosition getPlayerDimensionalPosition(PlayerEntity player) {
        Vector3d pos = player.getPositionVec();
        ResourceLocation dim = player.world.getDimensionKey().getLocation();

        return new DimensionalPosition(dim, pos);
    }

    public static void teleportPlayerOutOfMachine(World world, ServerPlayerEntity serverPlayer) {
        if(world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) world;

            Optional<CompactMachineData> machine = CompactMachineMemoryData.INSTANCE.getMachineContainingPosition(serverPlayer.getPositionVec());
            if(!machine.isPresent()) {
                serverPlayer.sendStatusMessage(
                        new TranslationTextComponent("not_inside_machine"),
                        true);

                return;
            }

            CompactMachineData machineInfo = machine.get();

            Optional<CompactMachinePlayerData> machinePlayers = CompactMachineMemoryData.INSTANCE.getPlayerData(machineInfo.getId());
            if(!machinePlayers.isPresent()) {
                // No player data for machine, wut
                CompactMachines.LOGGER.warn("Warning: Machine player data not set but machine registered, and player is inside. Machine ID: {}", machineInfo.getId());
                serverPlayer.sendStatusMessage(new TranslationTextComponent("ah_crap"), true);
                return;
            }

            Optional<DimensionalPosition> lastPos = machinePlayers.get().getExternalSpawn(serverPlayer);
            if (!lastPos.isPresent()) {
                // PANIC

                return;
            } else {
                DimensionalPosition p = lastPos.get();
                Vector3d bp = p.getPosition();
                ResourceLocation dimRL = p.getDimension();
                RegistryKey<World> key = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dimRL);

                ServerWorld outsideWorld = world.getServer().getWorld(key);

                machine.ifPresent(m -> {
                    serverPlayer.teleport(outsideWorld, bp.getX(), bp.getY(), bp.getZ(), serverPlayer.rotationYaw, serverPlayer.rotationPitch);
                    CompactMachinePlayerUtil.removePlayerFromMachine(serverPlayer, m.getId());
                });
            }
        }
    }
}
