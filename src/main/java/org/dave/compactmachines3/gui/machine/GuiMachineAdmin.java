package org.dave.compactmachines3.gui.machine;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import org.dave.compactmachines3.network.MessageRequestMachineAction;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.world.tools.TeleportationTools;

import java.io.IOException;

public class GuiMachineAdmin extends GuiMachine {
    private GuiButton buttonPrevious;
    private GuiButton buttonNext;
    private GuiButton buttonGiveItem;
    private GuiButton buttonTeleportOutside;
    private GuiButton buttonTeleportInside;

    @Override
    public void initGui() {
        super.initGui();

        int yOffset = 37;
        buttonPrevious = new GuiButton(Buttons.PREVIOUS.ordinal(), 4, height - 23, 80, 20, "Previous");
        buttonNext = new GuiButton(Buttons.NEXT.ordinal(), 4, height - 46, 80, 20, "Next");
        buttonGiveItem = new GuiButton(Buttons.GIVEITEM.ordinal(), 4, height - 92, 80, 20, TextFormatting.RED + "Give Item");
        buttonTeleportOutside = new GuiButton(Buttons.TELEPORT_OUTSIDE.ordinal(), 4, height - 115, 80, 20, "Teleport: out");
        buttonTeleportInside = new GuiButton(Buttons.TELEPORT_INSIDE.ordinal(), 4, height - 138, 80, 20, "Teleport: in");

        this.buttonList.add(buttonPrevious);
        this.buttonList.add(buttonNext);
        this.buttonList.add(buttonGiveItem);
        this.buttonList.add(buttonTeleportInside);
        this.buttonList.add(buttonTeleportOutside);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if(button.id == Buttons.PREVIOUS.ordinal()) {
            rotateX = 0.0f;
            rotateY = -25.0f;

            MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords-1, MessageRequestMachineAction.Action.REFRESH);
            PackageHandler.instance.sendToServer(requestMessage);
        } else if(button.id == Buttons.NEXT.ordinal()) {
            rotateX = 0.0f;
            rotateY = -25.0f;

            MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords+1, MessageRequestMachineAction.Action.REFRESH);
            PackageHandler.instance.sendToServer(requestMessage);
        } else if(button.id == Buttons.GIVEITEM.ordinal()) {
            MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.GIVE_ITEM);
            PackageHandler.instance.sendToServer(requestMessage);
        } else if(button.id == Buttons.TELEPORT_OUTSIDE.ordinal()) {
            MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TELEPORT_OUTSIDE);
            PackageHandler.instance.sendToServer(requestMessage);
        } else if(button.id == Buttons.TELEPORT_INSIDE.ordinal()) {
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
            mc.fontRenderer.drawString(String.format("Owner: %s", GuiMachineData.owner), 5, 30, 0xFFFFFFFF, true);
            mc.fontRenderer.drawString(String.format("Position: %d,%d,%d @ %d",
                    GuiMachineData.machinePos.getBlockPos().getX(),
                    GuiMachineData.machinePos.getBlockPos().getY(),
                    GuiMachineData.machinePos.getBlockPos().getZ(),
                    GuiMachineData.machinePos.getDimension()
            ), 5, 40, 0xFFFFFFFF, true);
        }

        if(buttonGiveItem.isMouseOver()) {
            drawHoveringText(I18n.format("commands.compactmachines3.machines.give.warning"), mouseX, mouseY);
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
