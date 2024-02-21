package dev.compactmods.machines.neoforge.network;

import net.minecraft.network.FriendlyByteBuf;

public record PlayerStartedRoomTrackingPacket(String room) {

    public PlayerStartedRoomTrackingPacket(FriendlyByteBuf buf) {
        this(buf.readUtf());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(room);
    }

//    public boolean handle(NetworkEvent.Context ctx) {
//        var sender = ctx.getSender();
//        ctx.enqueueWork(() -> {
//            StructureTemplate blocks;
//            try {
//                blocks = RoomBlocks.getInternalBlocks(sender.server, room).get(5, TimeUnit.SECONDS);
//            } catch (InterruptedException | ExecutionException | TimeoutException | MissingDimensionException e) {
//                throw new RuntimeException(e);
//            }
//            RoomNetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sender), new InitialRoomBlockDataPacket(blocks));
//        });
//
//        return true;
//    }
}
