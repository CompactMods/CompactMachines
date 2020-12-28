package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.particles.ParticleBlockMarker;

public class MessageParticleBlockMarker implements IMessage {
    double x;
    double y;
    double z;

    public MessageParticleBlockMarker() {
    }

    public MessageParticleBlockMarker(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        x = buf.readDouble();
        y = buf.readDouble();
        z = buf.readDouble();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public static class MessageParticleBlockMarkerHandler implements IMessageHandler<MessageParticleBlockMarker, MessageParticleBlockMarker> {

        @Override
        public MessageParticleBlockMarker onMessage(MessageParticleBlockMarker message, MessageContext ctx) {
            CompactMachines3.proxy.renderBlockMarker(message.x, message.y, message.z);
            return null;
        }
    }
}
