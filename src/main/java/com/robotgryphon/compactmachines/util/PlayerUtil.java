package com.robotgryphon.compactmachines.util;

import com.mojang.authlib.GameProfile;
import com.robotgryphon.compactmachines.reference.Reference;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
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

        Vector3d positionVec = player.getPositionVec();

        CompoundNBT pos = new CompoundNBT();
        pos.putDouble("x", positionVec.x);
        pos.putDouble("y", positionVec.y);
        pos.putDouble("z", positionVec.z);

        data.put(COMPACT_POSITION_NBT, pos);
    }

    public static Optional<Vector3d> getLastPosition(PlayerEntity player) {
        CompoundNBT data = player.getPersistentData();
        if(!data.contains(Reference.CompactMachines.COMPACT_POSITION_NBT))
            return Optional.empty();

        CompoundNBT pos = data.getCompound(Reference.CompactMachines.COMPACT_POSITION_NBT);
        Vector3d posV = new Vector3d(
                pos.getDouble("x"),
                pos.getDouble("y"),
                pos.getDouble("z")
        );

        return Optional.of(posV);
    }
}
