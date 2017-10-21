package org.dave.compactmachines3.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import org.dave.compactmachines3.gui.machine.GuiMachineData;

public class MessageMachineContentHandler implements IMessageHandler<MessageMachineContent, MessageMachineContent> {
    @Override
    public MessageMachineContent onMessage(MessageMachineContent message, MessageContext ctx) {
        GuiMachineData.rawData = message.data;
        GuiMachineData.machineSize = message.machineSize;
        GuiMachineData.coords = message.coords;
        GuiMachineData.machinePos = message.machinePos;
        GuiMachineData.owner = message.owner;
        GuiMachineData.customName = message.customName;

        return null;
    }
}
