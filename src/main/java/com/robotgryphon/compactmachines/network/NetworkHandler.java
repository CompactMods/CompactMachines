package com.robotgryphon.compactmachines.network;

import com.robotgryphon.compactmachines.CompactMachines;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel MAIN_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(CompactMachines.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
}
