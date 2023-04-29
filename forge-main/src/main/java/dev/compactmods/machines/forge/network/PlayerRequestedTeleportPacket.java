package dev.compactmods.machines.forge.network;

import dev.compactmods.machines.api.dimension.MissingDimensionException;
import dev.compactmods.machines.forge.CompactMachines;
import dev.compactmods.machines.forge.room.RoomHelper;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerRequestedTeleportPacket(GlobalPos machine, String room) {

    public PlayerRequestedTeleportPacket(FriendlyByteBuf buf) {
        this(buf.readWithCodec(GlobalPos.CODEC), buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeWithCodec(GlobalPos.CODEC, machine);
        buf.writeUtf(room);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            final var player = ctx.get().getSender();
            if (player != null) {
                try {
                    RoomHelper.teleportPlayerIntoMachine(player.level, player, machine, room);
                } catch (MissingDimensionException e) {
                    CompactMachines.LOGGER.error("Failed to teleport player into machine.", e);
                }
            }
        });

        return true;
    }
}
