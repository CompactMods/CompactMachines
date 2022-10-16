package dev.compactmods.machines.util;

import com.mojang.authlib.GameProfile;
import dev.compactmods.machines.advancement.AdvancementTriggers;
import dev.compactmods.machines.api.core.Messages;
import dev.compactmods.machines.core.CompactMachinesNet;
import dev.compactmods.machines.i18n.TranslationUtil;
import dev.compactmods.machines.location.SimpleTeleporter;
import dev.compactmods.machines.room.RoomHelper;
import dev.compactmods.machines.room.network.SyncRoomMetadataPacket;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nonnull;
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

    public static void howDidYouGetThere(@Nonnull ServerPlayer serverPlayer) {
        AdvancementTriggers.HOW_DID_YOU_GET_HERE.trigger(serverPlayer);

        serverPlayer.displayClientMessage(
                TranslationUtil.message(Messages.HOW_DID_YOU_GET_HERE),
                true
        );
    }

    public static void teleportPlayerToRespawnOrOverworld(MinecraftServer serv, @Nonnull ServerPlayer player) {
        ServerLevel level = Optional.ofNullable(serv.getLevel(player.getRespawnDimension())).orElse(serv.overworld());
        Vec3 worldPos = Vec3.atCenterOf(level.getSharedSpawnPos());

        if (player.getRespawnPosition() != null)
            worldPos = Vec3.atCenterOf(player.getRespawnPosition());

        player.changeDimension(level, SimpleTeleporter.to(worldPos));

        player.getCapability(RoomHelper.CURRENT_ROOM_META).ifPresent(provider -> {
            provider.clearCurrent();
            CompactMachinesNet.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                    new SyncRoomMetadataPacket("", Util.NIL_UUID));
        });
    }

    public static Vec2 getLookDirection(Player player) {
        return new Vec2(player.xRotO, player.yRotO);
    }
}
