package dev.compactmods.machines.client.machine;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.client.MachineToolbar;
import dev.compactmods.machines.machine.client.MachineUI;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.compactmods.machines.room.preview.client.gui.RoomPreviewPanel;
import dev.compactmods.machines.upgrades.client.RoomUpgradesPanel;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasPanels;
import dev.robotgryphon.screenlib.ui.api.UiHost;
import dev.robotgryphon.screenlib.ui.api.panel.Panel;
import dev.robotgryphon.screenlib.ui.api.panel.PanelType;
import dev.robotgryphon.screenlib.ui.api.panel.PanelTypes;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registers the machine screen's toggleable panels with screenlib (keys in {@link MachineToolbar}).
 * Lives in the main mod because it ties together panels from several features. Both need a bound
 * room; the machine screen only adds them then, sharing one slot so only one is open at a time.
 *
 * <p>The upgrades panel is the room's upgrade canvas when the menu hosts one (room upgrades enabled);
 * otherwise a {@link RoomUpgradesPanel} explaining why there's nothing to edit.
 */
public final class MachineScreenPanels {

    private MachineScreenPanels() {}

    public static final DeferredRegister<PanelType<?>> PANELS =
            DeferredRegister.create(PanelTypes.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    static {
        PANELS.register(MachineToolbar.ROOM_PREVIEW_PANEL.identifier().getPath(),
                () -> (PanelType<RoomPreviewPanel>) host -> {
                    final MachineUIMenu menu = boundMenu();
                    return new RoomPreviewPanel(host, menu.currentRoom().orElseThrow(), () -> MachineUI.core(menu));
                });

        PANELS.register(MachineToolbar.ROOM_UPGRADES_PANEL.identifier().getPath(),
                () -> (PanelType<Panel>) MachineScreenPanels::upgradesPanel);
    }

    private static final int CANVAS_WIDTH = 240;
    private static final int CANVAS_HEIGHT = 180;

    private static Panel upgradesPanel(UiHost host) {
        final MachineUIMenu menu = boundMenu();
        if (menu.canvasHost().isEmpty())
            return new RoomUpgradesPanel(menu.currentRoom().orElseThrow());

        final Panel canvas = CanvasPanels.forHost(menu, Component.literal("Room Upgrades")).create(host);
        canvas.setSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        // Canvas panels open themselves; this one starts hidden until toggled from the toolbar
        canvas.close();
        return canvas;
    }

    /** Panels are created while their machine screen initialises. */
    private static MachineUIMenu boundMenu() {
        return MachineUI.currentMenu()
                .filter(menu -> menu.currentRoom().isPresent())
                .orElseThrow(() -> new IllegalStateException("Machine panel created outside a bound machine's screen"));
    }
}
