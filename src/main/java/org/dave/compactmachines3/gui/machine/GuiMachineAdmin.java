package org.dave.compactmachines3.gui.machine;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.dave.compactmachines3.network.MessageRequestMachineAction;
import org.dave.compactmachines3.network.PackageHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiMachineAdmin extends GuiMachine {
    private GuiButton buttonPrevious;
    private GuiButton buttonNext;
    private GuiButton buttonGiveItem;
    private GuiButton buttonTeleportOutside;
    private GuiButton buttonTeleportInside;

    private List<GuiButton> adminButtons;
    private int buttonIdOffset = 3;

    @Override
    public void initGui() {
        super.initGui();

        buttonPrevious = new GuiButton(buttonIdOffset + Buttons.PREVIOUS.ordinal(), 4, height - 23, 80, 20, "Previous");
        buttonNext = new GuiButton(buttonIdOffset + Buttons.NEXT.ordinal(), 4, height - 46, 80, 20, "Next");
        buttonGiveItem = new GuiButton(buttonIdOffset + Buttons.GIVEITEM.ordinal(), 4, height - 92, 80, 20, TextFormatting.RED + "Give Item");
        buttonTeleportOutside = new GuiButton(buttonIdOffset + Buttons.TELEPORT_OUTSIDE.ordinal(), 4, height - 115, 80, 20, "Teleport: out");
        buttonTeleportInside = new GuiButton(buttonIdOffset + Buttons.TELEPORT_INSIDE.ordinal(), 4, height - 138, 80, 20, "Teleport: in");


        this.adminButtons = new ArrayList<>();
        this.adminButtons.add(buttonPrevious);
        this.adminButtons.add(buttonNext);
        this.adminButtons.add(buttonGiveItem);
        this.adminButtons.add(buttonTeleportInside);
        this.adminButtons.add(buttonTeleportOutside);

        this.buttonList.addAll(this.adminButtons);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if(button.id == buttonIdOffset + Buttons.PREVIOUS.ordinal()) {
            rotateX = 0.0f;
            rotateY = -25.0f;

            MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords-1, MessageRequestMachineAction.Action.REFRESH);
            PackageHandler.instance.sendToServer(requestMessage);
        } else if(button.id == buttonIdOffset + Buttons.NEXT.ordinal()) {
            rotateX = 0.0f;
            rotateY = -25.0f;

            MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords+1, MessageRequestMachineAction.Action.REFRESH);
            PackageHandler.instance.sendToServer(requestMessage);
        } else if(button.id == buttonIdOffset + Buttons.GIVEITEM.ordinal()) {
            MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.GIVE_ITEM);
            PackageHandler.instance.sendToServer(requestMessage);
        } else if(button.id == buttonIdOffset + Buttons.TELEPORT_OUTSIDE.ordinal()) {
            MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TELEPORT_OUTSIDE);
            PackageHandler.instance.sendToServer(requestMessage);
        } else if(button.id == buttonIdOffset + Buttons.TELEPORT_INSIDE.ordinal()) {
            MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TELEPORT_INSIDE);
            PackageHandler.instance.sendToServer(requestMessage);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        int x = (GuiMachineData.coords << 10) + GuiMachineData.machineSize / 2;
        int y = 41;
        int z = GuiMachineData.machineSize / 2;
        mc.fontRenderer.drawString(String.format("Machine: #%03d %s%s", GuiMachineData.coords, TextFormatting.YELLOW, GuiMachineData.customName == null ? "" : GuiMachineData.customName), 5, 5, 0xFFFFFFFF, true);
        mc.fontRenderer.drawString(String.format("Position: %d,%d,%d", x, y, z), 5, 15, 0xFFFFFFFF, true);
        if(GuiMachineData.machinePos != null) {
            mc.fontRenderer.drawString(String.format("Position: %d,%d,%d @ %d",
                    GuiMachineData.machinePos.getBlockPos().getX(),
                    GuiMachineData.machinePos.getBlockPos().getY(),
                    GuiMachineData.machinePos.getBlockPos().getZ(),
                    GuiMachineData.machinePos.getDimension()
            ), 5, 30, 0xFFFFFFFF, true);
        }

        for(GuiButton button : adminButtons) {
            button.drawButton(this.mc, mouseX, mouseY, partialTicks);
        }

        if(buttonGiveItem.isMouseOver()) {
            drawHoveringText(I18n.format("commands.compactmachines3.machines.give.warning"), mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        for(GuiButton button : adminButtons) {
            if (button.mousePressed(this.mc, mouseX, mouseY)) {
                button.playPressSound(this.mc.getSoundHandler());
                this.actionPerformed(button);
            }
        }
    }

    enum Buttons {
        PREVIOUS,
        NEXT,
        GIVEITEM,
        TELEPORT_OUTSIDE,
        TELEPORT_INSIDE
    }
}
