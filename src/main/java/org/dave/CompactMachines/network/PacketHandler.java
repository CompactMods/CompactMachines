package org.dave.CompactMachines.network;

import org.dave.CompactMachines.reference.Reference;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

public class PacketHandler {

	public static final SimpleNetworkWrapper	INSTANCE	= NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MOD_ID.toLowerCase());

	public static void init() {
		INSTANCE.registerMessage(MessageHoppingModeChange.class, MessageHoppingModeChange.class, 1, Side.SERVER);
		INSTANCE.registerMessage(MessagePlayerRotation.class, MessagePlayerRotation.class, 2, Side.CLIENT);
		INSTANCE.registerMessage(MessageConfiguration.class, MessageConfiguration.class, 3, Side.CLIENT);
	}

}
