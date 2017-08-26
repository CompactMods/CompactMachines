package org.dave.compactmachines3.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.gui.machine.GuiMachineChunkHolder;

public class MessageMachineContentHandler implements IMessageHandler<MessageMachineContent, MessageMachineContent> {
    @Override
    public MessageMachineContent onMessage(MessageMachineContent message, MessageContext ctx) {
        GuiMachineChunkHolder.rawData = message.data;

        return null;
    }
}
