package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.robotgryphon.screenlib.ui.api.layout.Placement;
import dev.robotgryphon.screenlib.ui.api.panel.Panel;
import dev.robotgryphon.screenlib.ui.api.panel.PanelType;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.ResourceKey;

import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Tiles the machine screen's views — room upgrades, machine configuration and the room preview — so
 * any of them can be open at once, e.g. to watch the preview while editing:
 * <ul>
 *     <li>The preview takes the right third when a canvas is open beside it; alone, it fills two
 *     thirds of the screen, centred.</li>
 *     <li>Open canvases share the rest, stacked top to bottom.</li>
 * </ul>
 * Views are re-tiled whenever one opens or closes ({@link #arrange}), except ones the player has moved
 * or resized, which keep their layout.
 */
public final class MachineViewLayout {

    private MachineViewLayout() {}

    /** The views, in tiling order. */
    public enum View {
        ROOM_UPGRADES(true),
        MACHINE_CONFIG(true),
        ROOM_PREVIEW(false);

        final boolean canvas;

        View(boolean canvas) {
            this.canvas = canvas;
        }

        /** {@return the view's panel} (looked up on use: the toolbar class registers buttons when loaded) */
        public ResourceKey<PanelType<?>> panel() {
            return switch (this) {
                case ROOM_UPGRADES -> MachineToolbar.ROOM_UPGRADES_PANEL;
                case MACHINE_CONFIG -> MachineToolbar.MACHINE_CONFIG_PANEL;
                case ROOM_PREVIEW -> MachineToolbar.ROOM_PREVIEW_PANEL;
            };
        }
    }

    private static final int GAP = 4;

    /** Each open machine screen's view panels, keyed by its menu. */
    private static final Map<MachineUIMenu, Map<View, Panel>> PANELS = new WeakHashMap<>();

    /** {@return the placement for {@code view}'s panel}, which also lets {@link #arrange} find it later */
    public static Placement placement(View view) {
        return Placement.of((element, screen) -> {
            if (!(element instanceof Panel panel))
                return;

            MachineUI.currentMenu().ifPresent(menu ->
                    PANELS.computeIfAbsent(menu, m -> new EnumMap<>(View.class)).put(view, panel));

            // A closed view is laid out as if it were opening, so it appears in the right spot
            final Set<View> open = open();
            open.add(view);
            apply(panel, tile(open, MachineUI.viewArea()).get(view));
        });
    }

    /** Re-tiles the open views of the current machine screen, except moved or resized ones. */
    public static void arrange() {
        MachineUI.currentMenu().map(PANELS::get).ifPresent(panels -> {
            final var layout = tile(open(), MachineUI.viewArea());
            panels.forEach((view, panel) -> {
                if (panel.isOpen() && !panel.isMoved())
                    apply(panel, layout.get(view));
            });
        });
    }

    /** {@return the current machine screen's open views} */
    private static Set<View> open() {
        final Set<View> open = new LinkedHashSet<>();
        MachineUI.currentMenu().map(PANELS::get).ifPresent(panels -> panels.forEach((view, panel) -> {
            if (panel.isOpen())
                open.add(view);
        }));
        return open;
    }

    /** {@return where each of the {@code open} views goes in {@code area}} */
    static Map<View, ScreenRectangle> tile(Set<View> open, ScreenRectangle area) {
        final Map<View, ScreenRectangle> layout = new EnumMap<>(View.class);
        final int canvases = (int) open.stream().filter(view -> view.canvas).count();
        final boolean preview = open.contains(View.ROOM_PREVIEW);

        if (preview && canvases == 0) {
            final int width = area.width() * 2 / 3, height = area.height() * 2 / 3;
            layout.put(View.ROOM_PREVIEW, new ScreenRectangle(area.left() + (area.width() - width) / 2,
                    area.top() + (area.height() - height) / 2, width, height));
            return layout;
        }

        int canvasWidth = area.width();
        if (preview) {
            final int previewWidth = area.width() / 3;
            canvasWidth = area.width() - previewWidth - GAP;
            layout.put(View.ROOM_PREVIEW, new ScreenRectangle(area.left() + canvasWidth + GAP, area.top(),
                    previewWidth, area.height()));
        }

        if (canvases > 0) {
            final int height = (area.height() - GAP * (canvases - 1)) / canvases;
            int index = 0;
            for (View view : View.values()) {
                if (view.canvas && open.contains(view)) {
                    layout.put(view, new ScreenRectangle(area.left(), area.top() + index * (height + GAP), canvasWidth, height));
                    index++;
                }
            }
        }
        return layout;
    }

    private static void apply(Panel panel, ScreenRectangle rect) {
        panel.setRectangle(rect.width(), rect.height(), rect.left(), rect.top());
    }
}
