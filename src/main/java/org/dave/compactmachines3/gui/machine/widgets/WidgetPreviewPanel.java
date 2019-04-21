package org.dave.compactmachines3.gui.machine.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.dave.compactmachines3.gui.framework.event.MouseClickEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;
import org.dave.compactmachines3.gui.framework.widgets.WidgetButton;
import org.dave.compactmachines3.gui.framework.widgets.WidgetPanel;
import org.dave.compactmachines3.gui.framework.widgets.WidgetTextBox;
import org.dave.compactmachines3.gui.machine.GuiMachineData;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.network.MessageRequestMachineAction;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.utility.ShrinkingDeviceUtils;

public class WidgetPreviewPanel extends WidgetPanel {

    public WidgetPreviewPanel(EntityPlayer player, int width, int height) {
        super();
        this.setWidth(width);
        this.setHeight(height);
        this.setId("PreviewPanel");

        WidgetMachinePreview preview = new WidgetMachinePreview();
        preview.setWidth(width);
        preview.setHeight(height);
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
            enterButton.setTooltipLines(I18n.format("gui.compactmachines3.compactsky.enter"));

            enterButton.addListener(MouseClickEvent.class, (event, widget) -> {
                boolean hasDevice = ShrinkingDeviceUtils.hasShrinkingDeviceInInventory(player);
                boolean validCoords = GuiMachineData.coords != -1;
                boolean isAllowedToEnter = GuiMachineData.isAllowedToEnter(player);
                if(!hasDevice || !validCoords || !isAllowedToEnter) {
                    return WidgetEventResult.CONTINUE_PROCESSING;
                }

                PackageHandler.instance.sendToServer(new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TRY_TO_ENTER));
                player.closeScreen();

                return WidgetEventResult.HANDLED;
            });

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
