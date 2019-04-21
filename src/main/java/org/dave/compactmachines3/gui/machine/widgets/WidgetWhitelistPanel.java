package org.dave.compactmachines3.gui.machine.widgets;

import net.minecraft.client.resources.I18n;
import org.dave.compactmachines3.gui.framework.event.GuiDataUpdatedEvent;
import org.dave.compactmachines3.gui.framework.event.MouseClickEvent;
import org.dave.compactmachines3.gui.framework.event.ValueChangedEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;
import org.dave.compactmachines3.gui.framework.widgets.*;
import org.dave.compactmachines3.gui.machine.GuiMachineData;
import org.dave.compactmachines3.network.MessagePlayerWhiteListToggle;
import org.dave.compactmachines3.network.MessageRequestMachineAction;
import org.dave.compactmachines3.network.PackageHandler;

import java.util.ArrayList;

public class WidgetWhitelistPanel extends WidgetPanel {
    WidgetPanel listPanel;

    public WidgetWhitelistPanel(int width, int height) {
        super();
        this.setWidth(width);
        this.setHeight(height);

        WidgetCheckbox checkbox = new WidgetCheckbox();
        checkbox.setX(6);
        checkbox.setY(6);
        checkbox.setValue(GuiMachineData.locked);
        checkbox.addListener(ValueChangedEvent.class, (event, widget) -> {
            PackageHandler.instance.sendToServer(new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TOGGLE_LOCKED));
            listPanel.setVisible(!GuiMachineData.locked);
            return WidgetEventResult.HANDLED;
        });

        this.add(checkbox);

        WidgetTextBox textBox = new WidgetTextBox(I18n.format("tooltip.compactmachines3.lockforotherplayers.checkbox"), 0x333333);
        textBox.setX(18);
        textBox.setY(8);
        textBox.setWidth(170);
        this.add(textBox);

        listPanel = new WidgetPanel();
        listPanel.setHeight(height-27);
        listPanel.setWidth(width-2);
        listPanel.setX(6);
        listPanel.setY(25);
        listPanel.setVisible(GuiMachineData.locked);

        WidgetTextBox textBoxWhitelist = new WidgetTextBox(I18n.format("gui.compactmachines3.compactsky.whitelist") + ":", 0x333333);
        textBoxWhitelist.setX(0);
        textBoxWhitelist.setY(0);
        textBoxWhitelist.setWidth(170);
        listPanel.add(textBoxWhitelist);

        WidgetInputField inputField = new WidgetInputField("nameToAddToWhitelist");
        inputField.setX(0);
        inputField.setY(162);
        inputField.setWidth(width-32);
        inputField.setHeight(18);

        listPanel.add(inputField);

        WidgetButton inputButton = new WidgetButton("+");
        inputButton.setX(width-30);
        inputButton.setY(162);
        inputButton.setWidth(18);
        inputButton.setHeight(18);

        inputButton.addListener(MouseClickEvent.class, (event, widget) -> {
            String playerName = inputField.getText();
            if(playerName.length() == 0) {
                return WidgetEventResult.HANDLED;
            }

            PackageHandler.instance.sendToServer(new MessagePlayerWhiteListToggle(GuiMachineData.coords, playerName));
            inputField.setText("");

            return WidgetEventResult.HANDLED;
        });

        listPanel.add(inputButton);

        WidgetList usernameList = new WidgetList();
        usernameList.setId("usernameList");
        usernameList.setX(0);
        usernameList.setY(11);
        usernameList.setWidth(width-12);
        usernameList.setHeight(height-62);

        this.addListener(GuiDataUpdatedEvent.class, (event, widget) -> {
            usernameList.clear();

            ArrayList<String> sortedPlayerNames = (ArrayList<String>) GuiMachineData.playerWhiteList.clone();
            sortedPlayerNames.sort(String::compareToIgnoreCase);

            for(String username : sortedPlayerNames) {
                WidgetWhitelistEntry entry = new WidgetWhitelistEntry(username, this.width);
                usernameList.addListEntry(entry);
                entry.getDeleteButton().addListener(MouseClickEvent.class, (clickEvent, clickWidget) -> {
                    PackageHandler.instance.sendToServer(new MessagePlayerWhiteListToggle(GuiMachineData.coords, username));
                    usernameList.deselect();

                    return WidgetEventResult.HANDLED;
                });
            }

            return WidgetEventResult.CONTINUE_PROCESSING;
        });

        listPanel.add(usernameList);

        this.add(listPanel);
    }


}
