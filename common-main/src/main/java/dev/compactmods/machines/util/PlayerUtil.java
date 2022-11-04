package dev.compactmods.machines.util;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.i18n.TranslationUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public abstract class PlayerUtil {

    public static Optional<GameProfile> getProfileByUUID(MinecraftServer server, UUID uuid) {
        final var player = server.getPlayerList().getPlayer(uuid);
        if (player == null) {
            var profile = new GameProfile(uuid, "Unknown");
            var p2 = server.getSessionService().fillProfileProperties(profile, false);
            return Optional.ofNullable(p2);
        }

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }

    public static Optional<GameProfile> getProfileByUUID(LevelAccessor world, UUID uuid) {
        final var player = world.getPlayerByUUID(uuid);
        if (player == null)
            return Optional.empty();

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }

    public static void howDidYouGetThere(@NotNull ServerPlayer serverPlayer) {
        AdvancementTriggers.HOW_DID_YOU_GET_HERE.trigger(serverPlayer);

        serverPlayer.displayClientMessage(
                TranslationUtil.message(Messages.HOW_DID_YOU_GET_HERE),
                true
        );
    }

    public static Vec2 getLookDirection(Player player) {
        return new Vec2(player.xRotO, player.yRotO);
    }
}
