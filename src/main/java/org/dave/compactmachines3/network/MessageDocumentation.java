package org.dave.compactmachines3.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.gui.psd.GuiPSDScreen;
import org.dave.compactmachines3.gui.psd.Pages;

public class MessageDocumentation implements IMessage, IMessageHandler<MessageDocumentation, IMessage> {
    @Override
    public void fromBytes(ByteBuf buf) {}

    @Override
    public void toBytes(ByteBuf buf) {}

    @Override
    public IMessage onMessage(MessageDocumentation message, MessageContext ctx) {
        FMLCommonHandler.instance().showGuiScreen(new GuiPSDScreen());
        return null;
    }
}
