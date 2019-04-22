package org.dave.compactmachines3.gui.machine.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.dave.compactmachines3.gui.framework.event.GuiDataUpdatedEvent;
import org.dave.compactmachines3.gui.framework.event.MouseClickEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;
import org.dave.compactmachines3.gui.framework.event.WidgetExecuteEvent;
import org.dave.compactmachines3.gui.framework.widgets.WidgetButton;
import org.dave.compactmachines3.gui.framework.widgets.WidgetInputField;
import org.dave.compactmachines3.gui.framework.widgets.WidgetPanel;
import org.dave.compactmachines3.gui.framework.widgets.WidgetTextBox;
import org.dave.compactmachines3.gui.machine.GuiMachineData;
import org.dave.compactmachines3.init.Itemss;
import org.dave.compactmachines3.network.MessageRequestMachineAction;
import org.dave.compactmachines3.network.MessageSetMachineName;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.utility.ShrinkingDeviceUtils;

public class WidgetPreviewPanel extends WidgetPanel {
    int coords;

    public WidgetPreviewPanel(EntityPlayer player, int width, int height, boolean adminMode) {
        super();
        this.setWidth(width);
        this.setHeight(height);
        this.setId("PreviewPanel");

        this.coords = GuiMachineData.coords;

        WidgetMachinePreview preview = new WidgetMachinePreview();
        preview.setWidth(width);
        preview.setHeight(height);
        this.add(preview);

        WidgetTextBox machineNameTextBox = new WidgetTextBox(GuiMachineData.customName, 0xFF1f2429);
        machineNameTextBox.setX(5);
        machineNameTextBox.setY(7);
        machineNameTextBox.setWidth(200);

        if(adminMode) {
            machineNameTextBox.setText(String.format("%d: %s", GuiMachineData.coords, GuiMachineData.customName));
        }

        this.add(machineNameTextBox);

        WidgetPanel renamePanel = new WidgetPanel();
        renamePanel.setY(5);
        renamePanel.setX(width-125);
        renamePanel.setWidth(120);
        renamePanel.setHeight(20);
        renamePanel.setVisible(false);

        WidgetButton confirmRenameButton = new WidgetButton("") {
            @Override
            protected void drawButtonContent(GuiScreen screen, FontRenderer renderer) {
                super.drawButtonContent(screen, renderer);

                screen.mc.renderEngine.bindTexture(BUTTON_TEXTURES);

                GlStateManager.enableBlend();
                screen.drawTexturedModalRect(3, 4, 76, 0, 14, 11);
                GlStateManager.disableBlend();
            }
        };
        confirmRenameButton.setX(100);
        confirmRenameButton.setWidth(20);
        confirmRenameButton.setHeight(20);
        renamePanel.add(confirmRenameButton);

        WidgetInputField renameInput = new WidgetInputField("renameInput");
        renameInput.setText(GuiMachineData.customName);
        renameInput.setWidth(98);
        renameInput.setHeight(20);
        renamePanel.add(renameInput);
        this.add(renamePanel);

        WidgetButton renameButton = new WidgetButton("") {
            @Override
            public void draw(GuiScreen screen) {
                this.drawButtonContent(screen, screen.mc.fontRenderer);
            }

            @Override
            protected void drawButtonContent(GuiScreen screen, FontRenderer renderer) {
                super.drawButtonContent(screen, renderer);

                screen.mc.getRenderItem().renderItemAndEffectIntoGUI(new ItemStack(Items.NAME_TAG), (width - 16) / 2, 1 );
            }
        };

        renameButton.setVisible(shouldShowRenamePanel(player, adminMode));
        renameButton.setX(width - 25);
        renameButton.setY(5);
        renameButton.setWidth(20);
        renameButton.setHeight(20);
        // TODO: Investigate why the tooltip wont show
        renameButton.setTooltipLines(I18n.format("gui.compactmachines3.compactsky.rename"));

        confirmRenameButton.addListener(MouseClickEvent.class, (event, widget) -> {
            renameButton.setVisible(true);
            machineNameTextBox.setVisible(true);
            renamePanel.setVisible(false);
            if(GuiMachineData.isUsedCube() && GuiMachineData.isOwner(player)) {
                PackageHandler.instance.sendToServer(new MessageSetMachineName(GuiMachineData.coords, renameInput.getText()));
            }
            return WidgetEventResult.HANDLED;
        });

        renameInput.addListener(WidgetExecuteEvent.class, (event, widget) -> {
            renameButton.setVisible(true);
            machineNameTextBox.setVisible(true);
            renamePanel.setVisible(false);
            if(GuiMachineData.isUsedCube() && GuiMachineData.isOwner(player)) {
                PackageHandler.instance.sendToServer(new MessageSetMachineName(GuiMachineData.coords, renameInput.getText()));
            }
            return WidgetEventResult.HANDLED;
        });

        renameButton.addListener(MouseClickEvent.class, (event, widget) -> {
            renameButton.setVisible(false);
            machineNameTextBox.setVisible(false);
            renamePanel.setVisible(true);
            renameInput.setCursorPositionEnd();
            renameInput.setSelectionPos(0);
            renameInput.setFocused(true);
            return WidgetEventResult.HANDLED;
        });

        this.add(renameButton);


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
        enterButton.setVisible(shouldShowEnterButton(player));
        // TODO: Investigate why the tooltip wont show
        enterButton.setTooltipLines(I18n.format("gui.compactmachines3.compactsky.enter"));

        enterButton.addListener(MouseClickEvent.class, (event, widget) -> {
            boolean hasDevice = ShrinkingDeviceUtils.hasShrinkingDeviceInInventory(player);
            boolean validCoords = GuiMachineData.isUsedCube();
            boolean isAllowedToEnter = GuiMachineData.isAllowedToEnter(player);
            if(!hasDevice || !validCoords || !isAllowedToEnter) {
                return WidgetEventResult.CONTINUE_PROCESSING;
            }

            PackageHandler.instance.sendToServer(new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TRY_TO_ENTER));
            player.closeScreen();

            return WidgetEventResult.HANDLED;
        });

        this.add(enterButton);

        WidgetTextBox ownerTextBox = new WidgetTextBox(getOwnerText(), 0xFF1f2429);
        ownerTextBox.setX(5);
        ownerTextBox.setY(height - 15);
        ownerTextBox.setWidth(200);

        this.add(ownerTextBox);

        WidgetTextBox positionBox = new WidgetTextBox(getPositionText(), 0xFF1f2429);
        positionBox.setX(5);
        positionBox.setY(17);
        positionBox.setWidth(200);
        positionBox.setVisible(adminMode);

        this.add(positionBox);


        this.addListener(GuiDataUpdatedEvent.class, (event, widget) -> {
            ownerTextBox.setText(getOwnerText());
            enterButton.setVisible(shouldShowEnterButton(player));

            machineNameTextBox.setText(GuiMachineData.customName);
            if(adminMode) {
                positionBox.setText(getPositionText());
                machineNameTextBox.setText(String.format("%d: %s", GuiMachineData.coords, GuiMachineData.customName));
            }

            if(this.coords != GuiMachineData.coords) {
                renameInput.setText("");
                machineNameTextBox.setVisible(true);
                renamePanel.setVisible(false);
                renameButton.setVisible(shouldShowRenamePanel(player, adminMode));

                this.coords = GuiMachineData.coords;
            }

            if(renameInput.getText().equals("")) {
                renameInput.setText(GuiMachineData.customName);
            }
            return WidgetEventResult.CONTINUE_PROCESSING;
        });
    }

    private String getOwnerText() {
        return GuiMachineData.owner != null ? GuiMachineData.owner : I18n.format("tooltip.compactmachines3.machine.coords.unused");
    }

    private String getPositionText() {
        if(GuiMachineData.machinePos == null) {
            return "Position: unknown";
        }

        return String.format("Position: %d,%d,%d @ %d",
                GuiMachineData.machinePos.getBlockPos().getX(),
                GuiMachineData.machinePos.getBlockPos().getY(),
                GuiMachineData.machinePos.getBlockPos().getZ(),
                GuiMachineData.machinePos.getDimension()
        );
    }

    private boolean shouldShowEnterButton(EntityPlayer player) {
        return ShrinkingDeviceUtils.hasShrinkingDeviceInInventory(player) && GuiMachineData.coords != -1 && GuiMachineData.isAllowedToEnter(player);
    }

    private boolean shouldShowRenamePanel(EntityPlayer player, boolean adminMode) {
        return GuiMachineData.isUsedCube() && (GuiMachineData.isOwner(player) || adminMode);
    }
}
