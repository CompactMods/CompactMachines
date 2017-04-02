package org.dave.cm2.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import org.dave.cm2.CompactMachines2;

public class PackageHandler {
    public static final SimpleNetworkWrapper instance = NetworkRegistry.INSTANCE.newSimpleChannel(CompactMachines2.MODID);

    public static void init() {
        instance.registerMessage(MessageEntitySizeChange.class, MessageEntitySizeChange.class, 1, Side.CLIENT);
        //instance.registerMessage(MessageApplyTinyPotionEffect.class, MessageApplyTinyPotionEffect.class, 2, Side.CLIENT);
    }
}
