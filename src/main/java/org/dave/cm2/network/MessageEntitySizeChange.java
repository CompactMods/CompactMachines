package org.dave.cm2.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.cm2.miniaturization.TinyPlayerPotion;
import org.dave.cm2.utility.Logz;


public class MessageEntitySizeChange implements IMessage, IMessageHandler<MessageEntitySizeChange, IMessage> {
    public int entityId;
    public float scale;



    /**
     * Convert from the supplied buffer into your specific message type
     *
     * @param buf
     */
    @Override
    public void fromBytes(ByteBuf buf) {
        entityId = buf.readInt();
        scale = buf.readFloat();
    }

    /**
     * Deconstruct your message into the supplied byte buffer
     *
     * @param buf
     */
    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeFloat(scale);
    }

    /**
     * Called when a message is received of the appropriate type. You can optionally return a reply message, or null if no reply
     * is needed.
     *
     * @param message The message
     * @param ctx
     * @return an optional return message
     */
    @Override
    public IMessage onMessage(MessageEntitySizeChange message, MessageContext ctx) {
        Logz.info("Received entity size change message. id=%d, size=%.2f", message.entityId, message.scale);
        Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(message.entityId);
        if(!(entity instanceof EntityPlayer)) {
            return null;
        }

        TinyPlayerPotion.setEntitySize((EntityPlayer) entity, message.scale);
        return null;
    }
}
