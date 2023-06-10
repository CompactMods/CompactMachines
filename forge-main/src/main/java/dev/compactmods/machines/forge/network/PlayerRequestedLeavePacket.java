package dev.compactmods.machines.forge.network;

import dev.compactmods.machines.forge.room.RoomHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record PlayerRequestedLeavePacket() {
    public PlayerRequestedLeavePacket(FriendlyByteBuf friendlyByteBuf) {
        this();
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        final var ctx = context.get();
        final var sender = ctx.getSender();

        RoomHelper.teleportPlayerOutOfRoom(sender);
    }

    public void encode(FriendlyByteBuf buffer) {

    }
}
