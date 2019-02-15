package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.CompactMachines3;

public class MessageWorldInfo implements IMessage, IMessageHandler<MessageWorldInfo, IMessage> {
    WorldInfo worldInfo;

    public MessageWorldInfo() {
    }

    public MessageWorldInfo(WorldInfo worldInfo) {
        this.worldInfo = worldInfo;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        NBTTagCompound worldInfoTag = ByteBufUtils.readTag(buf);
        this.worldInfo = new WorldInfo(worldInfoTag);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NBTTagCompound worldInfo = this.worldInfo.cloneNBTCompound(new NBTTagCompound());
        ByteBufUtils.writeTag(buf, worldInfo);
    }

    @Override
    public IMessage onMessage(MessageWorldInfo message, MessageContext ctx) {
        CompactMachines3.clientWorldData.init(message.worldInfo);
        return null;
    }
}
