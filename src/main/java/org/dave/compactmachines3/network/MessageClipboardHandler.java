package org.dave.compactmachines3.network;

import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class MessageClipboardHandler implements IMessageHandler<MessageClipboard, MessageClipboard> {

    @Override
    public MessageClipboard onMessage(MessageClipboard message, MessageContext ctx) {
        StringSelection selection = new StringSelection(message.clipboardContent);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        return null;
    }
}
