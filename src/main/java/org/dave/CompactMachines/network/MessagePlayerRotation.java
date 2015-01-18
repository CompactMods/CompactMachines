package org.dave.CompactMachines.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class MessagePlayerRotation implements IMessage, IMessageHandler<MessagePlayerRotation, IMessage> {
	float yaw;
	float pitch;

	public MessagePlayerRotation() {
	}

	public MessagePlayerRotation(float yaw, float pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
	}


	@Override
	public void fromBytes(ByteBuf buf) {
		yaw = buf.readFloat();
		pitch = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeFloat(yaw);
		buf.writeFloat(pitch);
	}

	@Override
	public IMessage onMessage(MessagePlayerRotation message, MessageContext ctx) {
		Minecraft.getMinecraft().thePlayer.rotationYaw = message.yaw;
		Minecraft.getMinecraft().thePlayer.rotationPitch = message.pitch;
		return null;
	}

}
