package org.dave.CompactMachines.network;

import io.netty.buffer.ByteBuf;

import org.dave.CompactMachines.handler.SharedStorageHandler;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessageHoppingModeChange implements IMessage, IMessageHandler<MessageHoppingModeChange, IMessage> {
	int	coord, side, hoppingMode;

	public MessageHoppingModeChange() {}

	public MessageHoppingModeChange(int coord, int side, int hoppingMode) {
		this.coord = coord;
		this.side = side;
		this.hoppingMode = hoppingMode;
	}

	@Override
	public IMessage onMessage(MessageHoppingModeChange message, MessageContext ctx) {
		SharedStorageHandler.instance(false).setHoppingModeForAll(message.coord, message.side, message.hoppingMode);
		return null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		coord = buf.readInt();
		side = buf.readInt();
		hoppingMode = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(coord);
		buf.writeInt(side);
		buf.writeInt(hoppingMode);
	}

}
