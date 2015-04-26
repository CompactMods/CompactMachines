package org.dave.CompactMachines.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.common.DimensionManager;

import org.dave.CompactMachines.handler.ConfigurationHandler;
import org.dave.CompactMachines.machines.world.WorldProviderMachines;
import org.dave.CompactMachines.utility.LogHelper;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageConfiguration implements IMessage, IMessageHandler<MessageConfiguration, IMessage> {

	public MessageConfiguration() {
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		LogHelper.info("Receiving configuration from server");
		ConfigurationHandler.isServerConfig = true;
		ConfigurationHandler.dimensionId = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(ConfigurationHandler.dimensionId);
	}

	@Override
	public IMessage onMessage(MessageConfiguration message, MessageContext ctx) {
		LogHelper.info("Registering dimension " + ConfigurationHandler.dimensionId + " on client side");
		DimensionManager.registerProviderType(ConfigurationHandler.dimensionId, WorldProviderMachines.class, true);
		DimensionManager.registerDimension(ConfigurationHandler.dimensionId, ConfigurationHandler.dimensionId);
		return null;
	}

}
