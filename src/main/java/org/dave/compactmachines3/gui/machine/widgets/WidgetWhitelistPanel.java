package org.dave.compactmachines3.gui.machine.widgets;

import net.minecraft.client.resources.I18n;
import org.dave.compactmachines3.gui.framework.widgets.*;
import org.dave.compactmachines3.gui.machine.GuiMachineData;

public class WidgetWhitelistPanel extends WidgetPanel {
    public WidgetWhitelistPanel(int width, int height) {
        WidgetCheckbox checkbox = new WidgetCheckbox();
        checkbox.setX(6);
        checkbox.setY(6);
        checkbox.setValue(GuiMachineData.locked);

        this.add(checkbox);

        WidgetTextBox textBox = new WidgetTextBox(I18n.format("tooltip.compactmachines3.lockforotherplayers.checkbox"), 0x333333);
        textBox.setX(18);
        textBox.setY(8);
        textBox.setWidth(170);
        this.add(textBox);

        WidgetInputField inputField = new WidgetInputField("nameToAddToWhitelist");
        inputField.setX(6);
        inputField.setY(20);
        inputField.setWidth(width-32);
        inputField.setHeight(18);

        this.add(inputField);

        WidgetButton inputButton = new WidgetButton("+");
        inputButton.setX(width-24);
        inputButton.setY(20);
        inputButton.setWidth(18);
        inputButton.setHeight(18);

        this.add(inputButton);
    }
}
