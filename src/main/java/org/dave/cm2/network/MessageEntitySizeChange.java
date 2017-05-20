package org.dave.cm2.network;

import io.netty.buffer.ByteBuf;
import mcjty.lib.tools.MinecraftTools;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.cm2.miniaturization.MiniaturizationPotion;


public class MessageEntitySizeChange implements IMessage, IMessageHandler<MessageEntitySizeChange, IMessage> {
    public int entityId;
    public float scale;

    public MessageEntitySizeChange() {
    }

    public MessageEntitySizeChange(int entityId, float scale) {
        this.entityId = entityId;
        this.scale = scale;
    }

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
        Entity entity = MinecraftTools.getWorld(Minecraft.getMinecraft()).getEntityByID(message.entityId);
        if(!(entity instanceof EntityPlayer)) {
            return null;
        }

        MiniaturizationPotion.setEntitySize((EntityPlayer) entity, message.scale);
        /*
        if(message.scale == 1.0f) {
            IAttributeInstance scaleAttribute = ((EntityPlayer) entity).getAttributeMap().getAttributeInstance(Potionss.scaleAttribute);
            if (scaleAttribute != null) {
                scaleAttribute.removeAllModifiers();
            }
        }
        */

        return null;
    }
}
