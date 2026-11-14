package dev.compactmods.machines.shrinking.network;

import dev.compactmods.machines.network.room.PlayerRequestedLeavePacket;
import dev.compactmods.machines.network.room.PlayerRequestedTeleportPacket;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/// Shrinking feature's network handler. Owns the room-metadata sync plus the
/// teleport/leave request packets (both drive shrinking-device room transit),
/// invoked by the mod's central payload-registration listener.
public final class ShrinkingNetworking {
    private ShrinkingNetworking() {}

    public static void register(final PayloadRegistrar reg) {
        reg.playToClient(SyncRoomMetadataPacket.TYPE, SyncRoomMetadataPacket.STREAM_CODEC, SyncRoomMetadataPacket.HANDLER);

        reg.playToServer(PlayerRequestedTeleportPacket.TYPE, PlayerRequestedTeleportPacket.STREAM_CODEC, PlayerRequestedTeleportPacket.HANDLER);
        reg.playToServer(PlayerRequestedLeavePacket.TYPE, StreamCodec.unit(new PlayerRequestedLeavePacket()), PlayerRequestedLeavePacket.HANDLER);
    }
}
