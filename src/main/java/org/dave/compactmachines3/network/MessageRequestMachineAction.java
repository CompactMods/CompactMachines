package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MessageRequestMachineAction implements IMessage {
    int coords;
    Action action;

    public MessageRequestMachineAction(int coords, Action action) {
        this.coords = coords;
        this.action = action;
    }

    public MessageRequestMachineAction() {
    }

    public void setCoords(int coords) {
        this.coords = coords;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.coords = buf.readInt();
        this.action = Action.values()[buf.readInt()];
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.coords);
        buf.writeInt(this.action.ordinal());
    }

    public enum Action {
        REFRESH,
        GIVE_ITEM,
        TELEPORT_INSIDE,
        TELEPORT_OUTSIDE,
        TOGGLE_LOCKED,
        TRY_TO_ENTER
    }
}
