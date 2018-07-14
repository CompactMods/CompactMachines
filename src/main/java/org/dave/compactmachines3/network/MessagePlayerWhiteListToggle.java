package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessagePlayerWhiteListToggle implements IMessage {
    int coords;
    String playerName;

    public MessagePlayerWhiteListToggle() {
    }

    public MessagePlayerWhiteListToggle(int coords, String playerName) {
        this.coords = coords;
        this.playerName = playerName;
    }

    public void setCoords(int coords) {
        this.coords = coords;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.playerName = ByteBufUtils.readUTF8String(buf);
        this.coords = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.playerName);
        buf.writeInt(this.coords);
    }
}
