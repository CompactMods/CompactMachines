package com.robotgryphon.compactmachines.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.IWorld;

import java.util.Optional;
import java.util.UUID;

public abstract class PlayerUtil {
    public static Optional<GameProfile> getProfileByUUID(IWorld world, UUID uuid) {
        GameProfile profile = world.getPlayerByUuid(uuid).getGameProfile();

        if (profile == null) {
            // Logz.warn("Profile not found for owner: %s", getOwner());
            return Optional.empty();
        }

        return Optional.of(profile);
    }
}
