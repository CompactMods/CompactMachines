package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageClipboard implements IMessage {
    protected String clipboardContent;

    public MessageClipboard() {
    }

    public void setClipboardContent(String clipboardContent) {
        this.clipboardContent = clipboardContent;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        clipboardContent = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, clipboardContent);
    }
}
