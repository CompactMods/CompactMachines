package dev.compactmods.machines.room.network;

import dev.compactmods.machines.CompactMachines;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.location.LevelBlockPosition;
import dev.compactmods.machines.room.RoomHelper;
import dev.compactmods.machines.room.graph.CompactRoomProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerRequestedTeleportPacket(LevelBlockPosition machine, String room) {

    public PlayerRequestedTeleportPacket(FriendlyByteBuf buf) {
        this(buf.readWithCodec(LevelBlockPosition.CODEC), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeWithCodec(LevelBlockPosition.CODEC, machine);
        buf.writeUtf(room);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final var player = ctx.get().getSender();
            if (player != null) {
                try {
                    final var provider = CompactRoomProvider.instance(CompactDimension.forServer(player.server));
                    provider.forRoom(room).ifPresent(info -> {
                        try {
                            RoomHelper.teleportPlayerIntoMachine(player.level, player, machine, info);
                        } catch (MissingDimensionException ignored) {
                        }
                    });
                } catch (MissingDimensionException e) {
                    CompactMachines.LOGGER.error("Failed to teleport player into machine.", e);
                }
            }
        });

        return true;
    }
}
