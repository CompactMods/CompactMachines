package dev.compactmods.machines.util;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.api.attachment.CMDataAttachments;
import dev.compactmods.machines.dimension.CompactDimensionTransitions;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.server.CompactMachinesServer;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
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

    public static void teleportPlayerToRespawnOrOverworld(MinecraftServer serv, @NotNull ServerPlayer player) {
        final var config = player.getRespawnConfig();

        final var transition = Optional.ofNullable(config)
                .map(c -> CompactDimensionTransitions.to(serv.getLevel(c.dimension()), Vec3.atBottomCenterOf(c.pos())))
                .orElse(CompactDimensionTransitions.to(serv.overworld(),
                        Vec3.atBottomCenterOf(serv.overworld().getSharedSpawnPos())));

        player.teleport(transition);
    }

    public static Optional<GameProfile> getProfileByUUID(MinecraftServer server, UUID uuid) {
        final var player = server.getPlayerList().getPlayer(uuid);
        if (player == null) {
            var p2 = server.getSessionService().fetchProfile(uuid, false);
            return p2 == null ? Optional.empty() : Optional.ofNullable(p2.profile());
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

    public static Vec2 getLookDirection(Player player) {
        return new Vec2(player.xRotO, player.yRotO);
    }

    public static void breakItemEffect(Player player, ItemStack stack) {
        if (!player.isSilent()) {

            var l = player.level();
            var pos = player.position();

            l.playSeededSound(
                    null,
                    pos.x(), pos.y(), pos.z(),
                    stack.getOrDefault(DataComponents.BREAK_SOUND, SoundEvents.ITEM_BREAK),
                    player.getSoundSource(),
                    1.0F,
                    0.8F + l.random.nextFloat() * 0.4F,
                    player.getRandom().nextLong()
            );
        }
    }
}
