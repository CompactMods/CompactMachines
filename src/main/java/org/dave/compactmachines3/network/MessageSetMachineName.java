package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageSetMachineName implements IMessage {
    int coords;
    String newName;

    public MessageSetMachineName() {
    }

    public MessageSetMachineName(int coords, String newName) {
        this.coords = coords;
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.newName = ByteBufUtils.readUTF8String(buf);
        this.coords = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.newName);
        buf.writeInt(this.coords);
    }
}
