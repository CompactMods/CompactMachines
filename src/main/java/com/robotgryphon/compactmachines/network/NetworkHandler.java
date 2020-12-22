package com.robotgryphon.compactmachines.network;

import com.robotgryphon.compactmachines.CompactMachines;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

public class NetworkHandler {
    private static int index = 0;
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel MAIN_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CompactMachines.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void initialize() {
        MAIN_CHANNEL.registerMessage(index++, MachinePlayersChangedPacket.class,
                MachinePlayersChangedPacket::encode, MachinePlayersChangedPacket::decode,
                MachinePlayersChangedPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        MAIN_CHANNEL.registerMessage(index++, TunnelAddedPacket.class,
                TunnelAddedPacket::encode, TunnelAddedPacket::decode,
                TunnelAddedPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
