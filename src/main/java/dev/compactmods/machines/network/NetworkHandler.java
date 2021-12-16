package dev.compactmods.machines.network;

import dev.compactmods.machines.CompactMachines;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "4.0.0";
    public static final SimpleChannel MAIN_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CompactMachines.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void initialize() {
//        MAIN_CHANNEL.messageBuilder(MachinePlayersChangedPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
//                .encoder(MachinePlayersChangedPacket::encode)
//                .decoder(MachinePlayersChangedPacket::new)
//                .consumer(MachinePlayersChangedPacket::handle)
//                .add();

        MAIN_CHANNEL.messageBuilder(TunnelAddedPacket.class, 1, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(TunnelAddedPacket::encode)
                .decoder(TunnelAddedPacket::new)
                .consumer(TunnelAddedPacket::handle)
                .add();
    }
}
