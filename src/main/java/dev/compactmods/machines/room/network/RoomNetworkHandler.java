package dev.compactmods.machines.room.network;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class RoomNetworkHandler {

    private static final ArtifactVersion ROOM_TRACK_VERSION = new DefaultArtifactVersion("1.0.0");

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "room_tracking"),
            ROOM_TRACK_VERSION::toString,
            clientVer -> VersionUtil.checkMajor(clientVer, ROOM_TRACK_VERSION),
            serverVer -> VersionUtil.checkMajor(serverVer, ROOM_TRACK_VERSION)
    );

    public static void setupMessages() {
        CHANNEL.messageBuilder(PlayerStartedRoomTrackingPacket.class, 1, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PlayerStartedRoomTrackingPacket::encode)
                .decoder(PlayerStartedRoomTrackingPacket::new)
                .consumerMainThread(PlayerStartedRoomTrackingPacket::handle)
                .add();

        CHANNEL.messageBuilder(InitialRoomBlockDataPacket.class, 2, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(InitialRoomBlockDataPacket::toNetwork)
                .decoder(InitialRoomBlockDataPacket::fromNetwork)
                .consumerMainThread(InitialRoomBlockDataPacket::handle)
                .add();
    }
}
