package org.dave.compactmachines3.gui.machine;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.compactmachines3.gui.framework.GUI;
import org.dave.compactmachines3.gui.framework.widgets.WidgetPanel;
import org.dave.compactmachines3.gui.framework.widgets.WidgetTabsPanel;
import org.dave.compactmachines3.gui.machine.widgets.WidgetPreviewPanel;
import org.dave.compactmachines3.gui.machine.widgets.WidgetWhitelistPanel;
import org.dave.compactmachines3.init.Blockss;

import java.util.Collections;

public class GuiMachineWidgetGui extends GUI {
    private World world;
    private BlockPos pos;
    private EntityPlayer player;

    public GuiMachineWidgetGui(int width, int height, World world, BlockPos pos, EntityPlayer player) {
        super(0, 0, width, height);
        this.world = world;
        this.pos = pos;
        this.player = player;

        WidgetTabsPanel tabs = new WidgetTabsPanel();
        tabs.setX(0);
        tabs.setY(0);
        tabs.setWidth(width);
        tabs.setHeight(height);

        tabs.addPage(createPreviewPanel(), new ItemStack(Blockss.wall), Collections.singletonList(I18n.format("gui.compactmachines3.compactsky.preview")));

        if(GuiMachineData.coords != -1 && GuiMachineData.isOwner(player)) {
            tabs.addPage(createWhitelistPanel(), new ItemStack(Items.FILLED_MAP), Collections.singletonList(I18n.format("gui.compactmachines3.compactsky.whitelist")));
        }

        this.add(tabs);

        WidgetPanel buttonPanel = tabs.getButtonsPanel();
        buttonPanel.setId("ButtonPanel");
        buttonPanel.setX(-32);
        buttonPanel.setY(0);
        buttonPanel.setWidth(40);
        buttonPanel.setHeight(80);

        this.add(buttonPanel);
    }

    public WidgetPreviewPanel createPreviewPanel() {
        return new WidgetPreviewPanel(player, this.width, this.height);
    }

    public WidgetWhitelistPanel createWhitelistPanel() {
        return new WidgetWhitelistPanel(this.width, this.height);
    }
}
