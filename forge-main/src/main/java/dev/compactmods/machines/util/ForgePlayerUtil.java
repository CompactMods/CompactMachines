package dev.compactmods.machines.util;

import dev.compactmods.machines.network.CompactMachinesNet;
import dev.compactmods.machines.dimension.SimpleTeleporter;
import dev.compactmods.machines.network.SyncRoomMetadataPacket;
import dev.compactmods.machines.room.RoomHelper;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ForgePlayerUtil {
    public static void teleportPlayerToRespawnOrOverworld(MinecraftServer serv, @NotNull ServerPlayer player) {
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
}
