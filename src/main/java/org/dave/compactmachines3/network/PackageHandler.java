package org.dave.compactmachines3.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import org.dave.compactmachines3.CompactMachines3;

public class PackageHandler {
    public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(CompactMachines3.MODID);

    public static void init() {
        instance.registerMessage(MessageMachineContentHandler.class, MessageMachineContent.class, 1, Side.CLIENT);
        instance.registerMessage(MessageClipboardHandler.class, MessageClipboard.class, 2, Side.CLIENT);
        instance.registerMessage(MessageRequestMachineActionHandler.class, MessageRequestMachineAction.class, 3, Side.SERVER);
    }
}

