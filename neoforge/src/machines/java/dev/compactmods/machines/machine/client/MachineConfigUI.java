package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.machine.ui.MachineConfigMenu;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasPanels;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasTools;
import dev.robotgryphon.screenlib.ui.api.UiScreenBuilder;
import dev.robotgryphon.screenlib.ui.api.UiScreens;
import dev.robotgryphon.screenlib.ui.api.layout.HorizontalAnchor;
import dev.robotgryphon.screenlib.ui.api.layout.Placement;
import dev.robotgryphon.screenlib.ui.api.layout.VerticalAnchor;
import dev.robotgryphon.screenlib.ui.api.panel.InventoryPanel;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarOrientation;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarTriggers;
import dev.robotgryphon.screenlib.ui.api.toolbar.Toolbars;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

/**
 * The machine configuration screen: a mirror of the machine screen's room upgrades view, editing the
 * machine's configuration canvas instead — named storages wired to the machine's sides. Register with
 * {@link UiScreens#register} as the {@link MachineConfigMenu} screen.
 */
public final class MachineConfigUI {

    private MachineConfigUI() {}

    private static final int MARGIN = 5;

    public static UiScreenBuilder create(MachineConfigMenu menu, Inventory inventory, Component title) {
        final var builder = UiScreens.builder(title);

        builder.layout((layout, host) -> layout.anchor(
                Toolbars.create(host, ToolbarOrientation.HORIZONTAL, MachineToolbar.CONFIG_ACTIONS, context -> true,
                        List.of(ToolbarTriggers.panels())),
                HorizontalAnchor.CENTER, VerticalAnchor.TOP, 0, MARGIN));

        builder.panel(Placement.anchored(HorizontalAnchor.CENTER, VerticalAnchor.CENTER), host -> {
            final var canvas = CanvasPanels.forHost(menu, title).create(host);
            final var full = MachineUI.fullPanelSize();
            canvas.setSize(full.width(), full.height());
            return canvas;
        });

        // Node palettes, one at a time on the left; after the canvas, since they look it up when created
        builder.tabs(Placement.anchored(HorizontalAnchor.LEFT, VerticalAnchor.CENTER, MARGIN, 0), MachineToolbar.UPGRADE_PALETTES);

        builder.panel(Placement.anchored(HorizontalAnchor.CENTER, VerticalAnchor.BOTTOM, 0, MARGIN), host -> {
            // Open from the start: there's no toolbar toggle for it on this screen
            final var panel = new InventoryPanel(menu, Component.translatable("container.inventory"));
            panel.open();
            return panel;
        });

        return builder;
    }
}
