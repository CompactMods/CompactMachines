package org.dave.compactmachines3.gui.machine;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.dave.compactmachines3.gui.framework.GUI;
import org.dave.compactmachines3.gui.framework.event.MouseClickEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;
import org.dave.compactmachines3.gui.framework.widgets.WidgetButton;
import org.dave.compactmachines3.gui.framework.widgets.WidgetPanel;
import org.dave.compactmachines3.gui.framework.widgets.WidgetTabsPanel;
import org.dave.compactmachines3.gui.machine.widgets.WidgetPreviewPanel;
import org.dave.compactmachines3.gui.machine.widgets.WidgetWhitelistPanel;
import org.dave.compactmachines3.init.Blockss;
import org.dave.compactmachines3.network.MessageRequestMachineAction;
import org.dave.compactmachines3.network.PackageHandler;

import java.util.Collections;

public class GuiMachineWidgetGui extends GUI {
    private World world;
    private BlockPos pos;
    private EntityPlayer player;

    public GuiMachineWidgetGui(int width, int height, World world, BlockPos pos, EntityPlayer player, boolean adminMode) {
        super(0, 0, width, height);
        this.world = world;
        this.pos = pos;
        this.player = player;

        if(adminMode) {
            WidgetButton giveItemButton = new WidgetButton("Give Item");
            giveItemButton.setWidth(60);
            giveItemButton.setX(-61);
            giveItemButton.setY(height-90);
            giveItemButton.addListener(MouseClickEvent.class, (event, widget) -> {
                player.closeScreen();
                MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.GIVE_ITEM);
                PackageHandler.instance.sendToServer(requestMessage);
                return WidgetEventResult.HANDLED;
            });
            giveItemButton.setTooltipLines(I18n.format("commands.compactmachines3.machines.give.warning"));
            this.add(giveItemButton);



            WidgetButton tpInsideButton = new WidgetButton("Teleport into machine");
            tpInsideButton.setWidth(110);
            tpInsideButton.setX(-111);
            tpInsideButton.setY(height-63);
            tpInsideButton.addListener(MouseClickEvent.class, (event, widget) -> {
                player.closeScreen();
                MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TELEPORT_INSIDE);
                PackageHandler.instance.sendToServer(requestMessage);
                return WidgetEventResult.HANDLED;
            });
            this.add(tpInsideButton);


            WidgetButton tpOutsideButton = new WidgetButton("Teleport to machine");
            tpOutsideButton.setWidth(110);
            tpOutsideButton.setX(-111);
            tpOutsideButton.setY(height-42);
            tpOutsideButton.addListener(MouseClickEvent.class, (event, widget) -> {
                player.closeScreen();
                MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords, MessageRequestMachineAction.Action.TELEPORT_OUTSIDE);
                PackageHandler.instance.sendToServer(requestMessage);
                return WidgetEventResult.HANDLED;
            });
            this.add(tpOutsideButton);


            WidgetButton previousButton = new WidgetButton("<");
            previousButton.setWidth(20);
            previousButton.setX(-42);
            previousButton.setY(height-21);
            previousButton.addListener(MouseClickEvent.class, (event, widget) -> {
                MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords-1, MessageRequestMachineAction.Action.REFRESH);
                PackageHandler.instance.sendToServer(requestMessage);
                return WidgetEventResult.HANDLED;
            });
            this.add(previousButton);

            WidgetButton nextButton = new WidgetButton(">");
            nextButton.setWidth(20);
            nextButton.setX(-21);
            nextButton.setY(height-21);
            nextButton.addListener(MouseClickEvent.class, (event, widget) -> {
                MessageRequestMachineAction requestMessage = new MessageRequestMachineAction(GuiMachineData.coords+1, MessageRequestMachineAction.Action.REFRESH);
                PackageHandler.instance.sendToServer(requestMessage);
                return WidgetEventResult.HANDLED;
            });

            this.add(nextButton);
        }

        WidgetTabsPanel tabs = new WidgetTabsPanel();
        tabs.setX(0);
        tabs.setY(0);
        tabs.setWidth(width);
        tabs.setHeight(height);

        tabs.addPage(new WidgetPreviewPanel(player, this.width, this.height, adminMode), new ItemStack(Blockss.wall), Collections.singletonList(I18n.format("gui.compactmachines3.compactsky.preview")));

        if(GuiMachineData.coords != -1 && GuiMachineData.isOwner(player)) {
            tabs.addPage(new WidgetWhitelistPanel(this.width, this.height), new ItemStack(Items.FILLED_MAP), Collections.singletonList(I18n.format("gui.compactmachines3.compactsky.whitelist")));
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
}
