package dev.compactmods.machines.util;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.dimension.CompactDimensionTransitions;
import dev.compactmods.machines.gamerule.CMGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

public abstract class PlayerUtil {

    public static void resetPlayerHistory(@NotNull ServerPlayer player) {
        final var history = CompactMachines.playerHistoryApi();
        player.removeData(CMDataAttachments.LAST_ROOM_ENTRYPOINT);
        history.entryPoints().clearHistory(player);
        history.save();
    }

    public static void handlePlayerMaybeEscaped(ServerPlayer player, GameRules rules) {
        boolean allowedOutOfBounds = switch (player.gameMode.getGameModeForPlayer()) {
            case GameType.ADVENTURE, GameType.SURVIVAL ->
                    rules.getBoolean(CMGameRules.ALLOW_SURVIVAL_OUT_OF_BOUNDS);
            case GameType.CREATIVE -> rules.getBoolean(CMGameRules.ALLOW_CREATIVE_OUT_OF_BOUNDS);
            case GameType.SPECTATOR -> rules.getBoolean(CMGameRules.ALLOW_SPECTATORS_OUT_OF_BOUNDS);
        };

        if (!allowedOutOfBounds)
            PlayerUtil.teleportPlayerToRespawnOrOverworld(player.server, player);
    }

    public static void teleportPlayerToRespawnOrOverworld(MinecraftServer serv, @NotNull ServerPlayer player) {
        ServerLevel level = Optional.ofNullable(serv.getLevel(player.getRespawnDimension())).orElse(serv.overworld());
        Vec3 worldPos = Vec3.atCenterOf(level.getSharedSpawnPos());

        if (player.getRespawnPosition() != null)
            worldPos = Vec3.atCenterOf(player.getRespawnPosition());

        player.changeDimension(CompactDimensionTransitions.to(level, worldPos));
    }

    public static Optional<GameProfile> getProfileByUUID(LevelAccessor world, UUID uuid) {
        final var player = world.getPlayerByUUID(uuid);
        if (player == null)
            return Optional.empty();

        GameProfile profile = player.getGameProfile();
        return Optional.of(profile);
    }

    public static void breakItemEffect(Player player, ItemStack stack) {
        if (!player.isSilent()) {

            var l = player.level();
            var pos = player.position();

            l.playSeededSound(
                    null,
                    pos.x(), pos.y(), pos.z(),
                    stack.getBreakingSound(),
                    player.getSoundSource(),
                    1.0F,
                    0.8F + l.random.nextFloat() * 0.4F,
                    player.getRandom().nextLong()
            );
        }
    }
}
