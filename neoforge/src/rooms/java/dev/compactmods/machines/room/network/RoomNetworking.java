package dev.compactmods.machines.room.network;

import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/// Room feature's network handler. Owns registration of the room packets;
/// invoked by the mod's central payload-registration listener (main → feature).
public final class RoomNetworking {
    private RoomNetworking() {}

    public static void register(final PayloadRegistrar reg) {
        reg.playToClient(RoomPreviewSnapshotPacket.TYPE, RoomPreviewSnapshotPacket.STREAM_CODEC, RoomPreviewSnapshotPacket.HANDLER);
        reg.playToClient(RoomEntitiesPacket.TYPE, RoomEntitiesPacket.STREAM_CODEC, RoomEntitiesPacket.HANDLER);
        reg.playToClient(RoomBlockEntitiesPacket.TYPE, RoomBlockEntitiesPacket.STREAM_CODEC, RoomBlockEntitiesPacket.HANDLER);
        reg.playToClient(RoomPreviewSpawnPacket.TYPE, RoomPreviewSpawnPacket.STREAM_CODEC, RoomPreviewSpawnPacket.HANDLER);

        reg.playToServer(RoomPreviewSubscribePacket.TYPE, RoomPreviewSubscribePacket.STREAM_CODEC, RoomPreviewSubscribePacket.HANDLER);
        reg.playToServer(PlayerStartedRoomTrackingPacket.TYPE, PlayerStartedRoomTrackingPacket.STREAM_CODEC, PlayerStartedRoomTrackingPacket.HANDLER);
        reg.playToServer(PlayerRequestedRoomUIPacket.TYPE, PlayerRequestedRoomUIPacket.STREAM_CODEC, PlayerRequestedRoomUIPacket.HANDLER);
    }
}
