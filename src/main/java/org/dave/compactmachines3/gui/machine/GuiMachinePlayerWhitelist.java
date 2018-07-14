package org.dave.compactmachines3.gui.machine;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;
import org.dave.compactmachines3.network.MessagePlayerWhiteListToggle;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.utility.Logz;

public class GuiMachinePlayerWhitelist extends GuiScrollingList {
    private final GuiContainer parent;
    public int selected = -1;

    public GuiMachinePlayerWhitelist(GuiContainer parent, int top, int bottom, int left, int entryHeight, int listWidth, int listHeight) {
        super(Minecraft.getMinecraft(), listWidth, listHeight, top, bottom, left, entryHeight, parent.width, parent.height);

        this.parent = parent;
    }

    @Override
    protected int getSize() {
        return GuiMachineData.playerWhiteList == null ? 0 : GuiMachineData.playerWhiteList.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        selected = index;

        if(doubleClick) {
            PackageHandler.instance.sendToServer(new MessagePlayerWhiteListToggle(GuiMachineData.coords, (GuiMachineData.playerWhiteList.get(selected))));
        }
    }

    @Override
    protected boolean isSelected(int index) {
        return index == selectedIndex;
    }

    @Override
    protected void drawBackground() {

    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        FontRenderer font = this.parent.mc.fontRenderer;

        font.drawString(GuiMachineData.playerWhiteList.get(slotIdx), this.left + 4, slotTop + 4, 0xFFFFFF);
    }
}
