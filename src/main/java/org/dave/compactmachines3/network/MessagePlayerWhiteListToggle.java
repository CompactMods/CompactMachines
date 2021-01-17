package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessagePlayerWhiteListToggle implements IMessage {
    int id;
    String playerName;

    public MessagePlayerWhiteListToggle() {
    }

    public MessagePlayerWhiteListToggle(int id, String playerName) {
        this.id = id;
        this.playerName = playerName;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerName = ByteBufUtils.readUTF8String(buf);
        this.id = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.playerName);
        buf.writeInt(this.id);
    }
}
