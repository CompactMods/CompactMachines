package org.dave.compactmachines3.gui.machine.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.dave.compactmachines3.gui.framework.widgets.WidgetButton;
import org.dave.compactmachines3.gui.framework.widgets.WidgetPanel;
import org.dave.compactmachines3.gui.framework.widgets.WidgetTextBox;
import org.dave.compactmachines3.gui.machine.GuiMachineData;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.utility.ShrinkingDeviceUtils;

public class WidgetPreviewPanel extends WidgetPanel {

    public WidgetPreviewPanel(EntityPlayer player, int width, int height) {
        this.setId("PreviewPanel");

        WidgetMachinePreview preview = new WidgetMachinePreview();
        preview.setX(width / 2);
        preview.setY(height / 2);
        this.add(preview);

        if(ShrinkingDeviceUtils.hasShrinkingDeviceInInventory(player) && GuiMachineData.coords != -1 && GuiMachineData.isAllowedToEnter(player)) {
            WidgetButton enterButton = new WidgetButton("") {
                @Override
                protected void drawButtonContent(GuiScreen screen, FontRenderer renderer) {
                    super.drawButtonContent(screen, renderer);

                    screen.mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Itemss.psd), (width - 16) / 2, 1 );
                }
            };
            enterButton.setX(width - 25);
            enterButton.setY(height - 25);
            enterButton.setWidth(20);
            enterButton.setHeight(20);
            // TODO: Investigate why the tooltip wont show
            enterButton.setTooltipLines("Enter machine");

            this.add(enterButton);
        }

        String ownerText = GuiMachineData.owner != null ? GuiMachineData.owner : I18n.format("tooltip.compactmachines3.machine.coords.unused");

        WidgetTextBox ownerTextBox = new WidgetTextBox(ownerText, 0xFF1f2429);
        ownerTextBox.setX(5);
        ownerTextBox.setY(height - 15);
        ownerTextBox.setWidth(200);

        this.add(ownerTextBox);
    }
}
