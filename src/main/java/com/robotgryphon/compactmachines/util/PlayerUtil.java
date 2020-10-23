package com.robotgryphon.compactmachines.util;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.reference.Reference;
import com.robotgryphon.compactmachines.teleportation.DimensionalPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

import java.util.Optional;
import java.util.UUID;

import static com.robotgryphon.compactmachines.reference.Reference.CompactMachines.COMPACT_POSITION_NBT;

public abstract class PlayerUtil {
    public static Optional<GameProfile> getProfileByUUID(IWorld world, UUID uuid) {
        PlayerEntity player = world.getPlayerByUuid(uuid);
        if(player == null)
            return Optional.empty();

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }

    public static void setLastPosition(PlayerEntity player) {
        CompoundNBT data = player.getPersistentData();

        Vector3d pos = player.getPositionVec();
        ResourceLocation dim = player.world.getDimensionKey().getLocation();

        DimensionalPosition dp = new DimensionalPosition(dim, pos);

        CompoundNBT dimNbt = dp.serializeNBT();

        data.put(COMPACT_POSITION_NBT, dimNbt);
    }

    public static Optional<DimensionalPosition> getLastPosition(PlayerEntity player) {
        CompoundNBT data = player.getPersistentData();
        if(!data.contains(Reference.CompactMachines.COMPACT_POSITION_NBT))
            return Optional.empty();

        CompoundNBT pos = data.getCompound(Reference.CompactMachines.COMPACT_POSITION_NBT);

        return Optional.of(DimensionalPosition.fromNBT(pos));
    }
}
