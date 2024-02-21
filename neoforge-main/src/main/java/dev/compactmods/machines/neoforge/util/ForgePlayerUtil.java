package dev.compactmods.machines.neoforge.util;

import dev.compactmods.machines.neoforge.dimension.SimpleTeleporter;
import dev.compactmods.machines.neoforge.room.Rooms;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ForgePlayerUtil {
    public static void teleportPlayerToRespawnOrOverworld(MinecraftServer serv, @NotNull ServerPlayer player) {
        ServerLevel level = Optional.ofNullable(serv.getLevel(player.getRespawnDimension())).orElse(serv.overworld());
        Vec3 worldPos = Vec3.atCenterOf(level.getSharedSpawnPos());

        if (player.getRespawnPosition() != null)
            worldPos = Vec3.atCenterOf(player.getRespawnPosition());

        player.changeDimension(level, SimpleTeleporter.to(worldPos));

        player.removeData(Rooms.LAST_ROOM_ENTRYPOINT);
    }
}
