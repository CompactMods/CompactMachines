package dev.compactmods.machines.core.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.LevelAccessor;

import java.util.Optional;
import java.util.UUID;

public abstract class ProfileUtil {
    public static Optional<GameProfile> getProfileByUUID(LevelAccessor world, UUID uuid) {
        final var player = world.getPlayerByUUID(uuid);
        if (player == null)
            return Optional.empty();

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }
}
