package dev.compactmods.machines.core;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.room.network.PlayerRequestedTeleportPacket;
import dev.compactmods.machines.tunnel.network.TunnelAddedPacket;
import dev.compactmods.machines.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

public class CompactMachinesNet {
    private static final ArtifactVersion PROTOCOL_VERSION;


    static {
        if(FMLEnvironment.production) {
            PROTOCOL_VERSION = new DefaultArtifactVersion(ModList.get().getModFileById(Constants.MOD_ID).versionString());
        } else {
            PROTOCOL_VERSION = new DefaultArtifactVersion("9.99.999");
        }
    }

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Constants.MOD_ID, "main"),
            PROTOCOL_VERSION::toString,
            clientVer -> VersionUtil.checkMajor(clientVer, PROTOCOL_VERSION),
            serverVer -> VersionUtil.checkMajor(serverVer, PROTOCOL_VERSION)
    );


    public static void setupMessages() {
        CHANNEL.messageBuilder(TunnelAddedPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TunnelAddedPacket::encode)
                .decoder(TunnelAddedPacket::new)
                .consumerMainThread(TunnelAddedPacket::handle)
                .add();

        CHANNEL.messageBuilder(PlayerRequestedTeleportPacket.class, 2, NetworkDirection.PLAY_TO_SERVER)
                .encoder(PlayerRequestedTeleportPacket::encode)
                .decoder(PlayerRequestedTeleportPacket::new)
                .consumerMainThread(PlayerRequestedTeleportPacket::handle)
                .add();
    }
}
