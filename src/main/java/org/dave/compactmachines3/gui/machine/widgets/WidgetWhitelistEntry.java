package org.dave.compactmachines3.gui.machine.widgets;

import net.minecraft.util.ResourceLocation;
import org.dave.compactmachines3.gui.framework.ISelectable;
import org.dave.compactmachines3.gui.framework.widgets.WidgetButton;
import org.dave.compactmachines3.gui.framework.widgets.WidgetPanel;
import org.dave.compactmachines3.gui.framework.widgets.WidgetTextBox;

public class WidgetWhitelistEntry extends WidgetPanel implements ISelectable {
    int padding = 1;
    int textColor = 0xFFFFFFFF;

    boolean isSelected = false;
    ResourceLocation background = new ResourceLocation("minecraft", "textures/blocks/redstone_block.png");

    WidgetButton deleteButton;

    public WidgetWhitelistEntry(String text, int width) {
        super();

        this.setWidth(width-16);
        this.setHeight(10 + 2*padding);

        WidgetTextBox textBox = new WidgetTextBox(text, textColor);
        textBox.setY(padding);
        textBox.setX(padding);
        textBox.setWidth(this.width-16);
        this.add(textBox);

        deleteButton = new WidgetButton("x");
        deleteButton.setBackgroundTexture(background);
        deleteButton.setY(0);
        deleteButton.setX(this.width-10);
        deleteButton.setHeight(9);
        deleteButton.setWidth(10);
        deleteButton.setVisible(false);

        this.add(deleteButton);
    }

    public WidgetButton getDeleteButton() {
        return deleteButton;
    }

    @Override
    public boolean isSelected() {
        return this.isSelected;
    }

    @Override
    public void setSelected(boolean state) {
        this.isSelected = state;

        deleteButton.setVisible(state);
    }
}
