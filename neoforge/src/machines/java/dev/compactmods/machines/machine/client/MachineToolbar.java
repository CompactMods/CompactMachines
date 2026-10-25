package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.MachineRoomEntry;
import dev.compactmods.machines.machine.network.MachineEnterRoomPacket;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.robotgryphon.screenlib.nodes.api.canvas.CanvasTools;
import dev.robotgryphon.screenlib.ui.api.panel.PanelType;
import dev.robotgryphon.screenlib.ui.api.panel.PanelTypes;
import dev.robotgryphon.screenlib.ui.api.toolbar.IconToolbarButton;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarButton;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarButtons;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarContext;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarIcons;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * The machine screen's toolbars. {@link #VIEWS} switches the main panel between the room preview and
 * room upgrades (exactly one is open); beside it, {@link #ROOM_ACTIONS} ("enter room") shows while the
 * preview is open and {@link #UPGRADE_ACTIONS} (the upgrade canvas's node palettes) while the upgrades
 * canvas is. Buttons are registered in screenlib's toolbar button registry (client only); the panels
 * they toggle are registered by the main mod, which can see every feature's panel class.
 */
public final class MachineToolbar {

    private MachineToolbar() {}

    public static final ResourceKey<PanelType<?>> ROOM_PREVIEW_PANEL =
            ResourceKey.create(PanelTypes.REGISTRY_KEY, CompactMachinesCore.identifier("room_preview"));
    public static final ResourceKey<PanelType<?>> ROOM_UPGRADES_PANEL =
            ResourceKey.create(PanelTypes.REGISTRY_KEY, CompactMachinesCore.identifier("room_upgrades"));

    /** Screenlib's node palettes for the upgrade canvas, shown one at a time beside it. */
    public static final List<ResourceKey<PanelType<?>>> UPGRADE_PALETTES =
            List.of(CanvasTools.ADD_NODE_PANEL, CanvasTools.PROVIDED_PANEL);

    /** The player's inventory, for picking up items to set on the canvas (e.g. an item property). */
    public static final ResourceKey<PanelType<?>> INVENTORY_PANEL =
            ResourceKey.create(PanelTypes.REGISTRY_KEY, CompactMachinesCore.identifier("inventory"));

    /** Panels that belong to the upgrades view, closed when leaving it. */
    private static final List<ResourceKey<PanelType<?>>> UPGRADE_VIEW_PANELS =
            List.of(CanvasTools.ADD_NODE_PANEL, CanvasTools.PROVIDED_PANEL, INVENTORY_PANEL);

    public static final DeferredRegister<ToolbarButton> BUTTONS =
            DeferredRegister.create(ToolbarButtons.REGISTRY_KEY, CompactMachinesCore.MOD_ID);

    static {
        BUTTONS.register("room_preview", () -> new MachineToolbarButton(
                CompactMachinesCore.identifier("room_preview_btn"),
                menu -> menu.currentRoom().isPresent()
                        ? Component.literal("Room preview")
                        : Component.literal("Room preview (no room bound)"),
                menu -> menu.currentRoom().isPresent(),
                (context, menu) -> context.isPanelOpen(ROOM_PREVIEW_PANEL),
                (context, menu) -> show(context, ROOM_PREVIEW_PANEL)));

        BUTTONS.register("room_upgrades", () -> new MachineToolbarButton(
                CompactMachinesCore.identifier("upgrade_btn"),
                menu -> menu.currentRoom().isPresent()
                        ? Component.literal("Room upgrades")
                        : Component.literal("Room upgrades (no room bound)"),
                menu -> menu.currentRoom().isPresent(),
                (context, menu) -> context.isPanelOpen(ROOM_UPGRADES_PANEL),
                (context, menu) -> show(context, ROOM_UPGRADES_PANEL)));

        BUTTONS.register("enter_room", () -> new MachineToolbarButton(
                CompactMachinesCore.identifier("personal_shrinking_device"),
                MachineToolbar::enterTooltip,
                menu -> menu.currentRoom().isPresent() && canEnter(),
                (context, menu) -> false,
                (context, menu) -> {
                    // The server re-checks everything; close the screen as the player shrinks into the room
                    ClientPacketDistributor.sendToServer(new MachineEnterRoomPacket(menu.machinePos()));
                    final var player = Minecraft.getInstance().player;
                    if (player != null)
                        player.closeContainer();
                }));

        BUTTONS.register("add_node", () -> IconToolbarButton.panelTab(
                ToolbarIcons.ADD_NODE, Component.literal("Add node"), CanvasTools.ADD_NODE_PANEL));

        BUTTONS.register("inventory", () -> new ItemToolbarButton(
                Items.CHEST, Component.translatable("container.inventory"), INVENTORY_PANEL));
    }

    public static final ResourceKey<ToolbarButton> ROOM_PREVIEW_BUTTON = key("room_preview");
    public static final ResourceKey<ToolbarButton> ROOM_UPGRADES_BUTTON = key("room_upgrades");
    public static final ResourceKey<ToolbarButton> ENTER_ROOM_BUTTON = key("enter_room");
    public static final ResourceKey<ToolbarButton> ADD_NODE_BUTTON = key("add_node");
    public static final ResourceKey<ToolbarButton> INVENTORY_BUTTON = key("inventory");

    /** Switches the main panel between the room preview and room upgrades. */
    public static final List<List<ResourceKey<ToolbarButton>>> VIEWS =
            List.of(List.of(ROOM_PREVIEW_BUTTON, ROOM_UPGRADES_BUTTON));

    /** Shown with the room preview. */
    public static final List<List<ResourceKey<ToolbarButton>>> ROOM_ACTIONS =
            List.of(List.of(ENTER_ROOM_BUTTON));

    /** Shown with the room upgrades canvas. */
    public static final List<List<ResourceKey<ToolbarButton>>> UPGRADE_ACTIONS =
            List.of(List.of(ADD_NODE_BUTTON, CanvasTools.PROVIDED), List.of(INVENTORY_BUTTON));

    public static boolean roomActionsShown(ToolbarContext context) {
        return context.isPanelOpen(ROOM_PREVIEW_PANEL);
    }

    /** Only with an actual canvas — the upgrades panel falls back to a message without one. */
    public static boolean upgradeActionsShown(ToolbarContext context) {
        return context.isPanelOpen(ROOM_UPGRADES_PANEL)
                && MachineUI.currentMenu().flatMap(MachineUIMenu::canvasHost).isPresent();
    }

    /**
     * Opens {@code panel} as the main panel. It shares a slot with the other one, so opening it closes
     * that; leaving the upgrades view also closes its palettes and the inventory.
     */
    private static void show(ToolbarContext context, ResourceKey<PanelType<?>> panel) {
        if (context.isPanelOpen(panel))
            return;

        if (!panel.equals(ROOM_UPGRADES_PANEL)) {
            for (ResourceKey<PanelType<?>> side : UPGRADE_VIEW_PANELS)
                if (context.isPanelOpen(side))
                    context.togglePanel(side);
        }

        context.togglePanel(panel);
    }

    private static ResourceKey<ToolbarButton> key(String name) {
        return ResourceKey.create(ToolbarButtons.REGISTRY_KEY, CompactMachinesCore.identifier(name));
    }

    private static boolean canEnter() {
        final var player = Minecraft.getInstance().player;
        return player != null && MachineRoomEntry.mayEnter(player);
    }

    private static Component enterTooltip(MachineUIMenu menu) {
        if (menu.currentRoom().isEmpty())
            return Component.literal("Enter room (no room bound)");
        if (!canEnter())
            return Component.literal("Enter room (requires a Personal Shrinking Device)");
        return Component.literal("Enter room");
    }
}
