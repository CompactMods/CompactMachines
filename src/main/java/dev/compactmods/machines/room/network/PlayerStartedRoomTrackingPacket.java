package dev.compactmods.machines.room.network;

import dev.compactmods.machines.core.MissingDimensionException;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.room.exceptions.NonexistentRoomException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.UUID;
import java.util.function.Supplier;

public record PlayerStartedRoomTrackingPacket(ChunkPos room) {

    public PlayerStartedRoomTrackingPacket(FriendlyByteBuf buf) {
        this(buf.readChunkPos());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeChunkPos(room);
    }

    public boolean handle(Supplier<NetworkEvent.Context> ctx) {
        var sender = ctx.get().getSender();
        ctx.get().enqueueWork(() -> {
            try {
                var blocks = Rooms.getInternalBlocks(sender.server, room);
                RoomNetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new InitialRoomBlockDataPacket(blocks));
            } catch (MissingDimensionException | NonexistentRoomException e) {
                e.printStackTrace();
            }
        });

        return true;
    }
}
