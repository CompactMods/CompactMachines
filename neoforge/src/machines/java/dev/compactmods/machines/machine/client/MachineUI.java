package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.robotgryphon.screenlib.ui.api.UiScreenBuilder;
import dev.robotgryphon.screenlib.ui.api.UiScreens;
import dev.robotgryphon.screenlib.ui.api.layout.HorizontalAnchor;
import dev.robotgryphon.screenlib.ui.api.layout.Placement;
import dev.robotgryphon.screenlib.ui.api.layout.UiLayout;
import dev.robotgryphon.screenlib.ui.api.layout.VerticalAnchor;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarOrientation;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarTriggers;
import dev.robotgryphon.screenlib.ui.api.toolbar.Toolbars;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * The machine screen, built with screenlib. Toolbars along the top switch between the room preview
 * and room upgrades panels, with actions for whichever is open (see {@link MachineToolbar}). A
 * machine bound to a room opens on its live room preview panel (with the core in the panel's corner); otherwise an info panel
 * describes the core — e.g. the template a pending room will be created from. Register with
 * {@link UiScreens#register} as the {@link MachineUIMenu} screen.
 *
 * <p>Screenlib's menu screens don't draw or handle slots, so the core is inserted/removed through the
 * block itself (use a core on it / sneak-use with an empty hand).
 */
public final class MachineUI {

    private MachineUI() {}

    private static final int MARGIN = 5;
    /** Space between the views toolbar and the action toolbar beside it. */
    private static final int GAP = 4;

    public static UiScreenBuilder create(MachineUIMenu menu, Inventory inventory, Component title) {
        final var builder = UiScreens.builder(title);
        final var center = Placement.anchored(HorizontalAnchor.CENTER, VerticalAnchor.CENTER);

        builder.layout((layout, host) -> {
            // The views' enabled state only depends on the room binding, fixed while the screen is open
            final var views = layout.anchor(
                    Toolbars.create(host, ToolbarOrientation.HORIZONTAL, MachineToolbar.VIEWS),
                    HorizontalAnchor.CENTER, VerticalAnchor.TOP, 0, MARGIN);

            // The action toolbars share the spot right of the views toolbar; at most one is shown
            // Room actions also watch the inventory: "enter room" needs a shrinking device
            besideViews(layout, views, Toolbars.create(host, ToolbarOrientation.HORIZONTAL,
                    MachineToolbar.ROOM_ACTIONS, MachineToolbar::roomActionsShown,
                    List.of(ToolbarTriggers.panels(), ToolbarTriggers.inventory())));
            besideViews(layout, views, Toolbars.create(host, ToolbarOrientation.HORIZONTAL,
                    MachineToolbar.UPGRADE_ACTIONS, MachineToolbar::upgradeActionsShown,
                    List.of(ToolbarTriggers.panels())));
        });

        if (menu.currentRoom().isPresent()) {
            // One slot, so opening either main panel closes the other (the preview opens itself)
            builder.panels(center, List.of(MachineToolbar.ROOM_PREVIEW_PANEL, MachineToolbar.ROOM_UPGRADES_PANEL));

            // Screenlib's node palettes for the upgrade canvas. After the main slot: they look up the
            // canvas panel when they're created.
            if (menu.canvasHost().isPresent())
                builder.tabs(Placement.anchored(HorizontalAnchor.LEFT, VerticalAnchor.CENTER, MARGIN, 0),
                        MachineToolbar.UPGRADE_PALETTES);
        } else {
            builder.panel(center, host -> new MachineInfoPanel(menu));
        }

        return builder;
    }

    private static void besideViews(UiLayout layout, AbstractWidget views, AbstractWidget toolbar) {
        // Centre-anchored, offset so its left edge sits GAP right of the views toolbar's right edge
        final int offset = views.getWidth() / 2 + GAP + toolbar.getWidth() / 2;
        layout.anchor(toolbar, HorizontalAnchor.CENTER, VerticalAnchor.TOP, offset, MARGIN);
    }

    /**
     * {@return the machine menu the local player has open}, if any. Toolbar buttons and panel types
     * are registry singletons, so this is how they reach the screen's menu.
     */
    public static Optional<MachineUIMenu> currentMenu() {
        final var player = Minecraft.getInstance().player;
        return player != null && player.containerMenu instanceof MachineUIMenu menu
                ? Optional.of(menu)
                : Optional.empty();
    }

    public static ItemStack core(MachineUIMenu menu) {
        final var slot = menu.getCoreSlot();
        return slot != null ? slot.getItem() : ItemStack.EMPTY;
    }
}
