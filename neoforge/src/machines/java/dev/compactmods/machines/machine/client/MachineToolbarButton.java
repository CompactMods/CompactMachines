package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarButton;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An icon button for the machine screen's toolbar, bound to the open {@link MachineUIMenu} (see
 * {@link MachineUI#currentMenu()}). Unlike screenlib's {@code IconToolbarButton} it can be disabled:
 * it then draws dimmed, ignores clicks, and its tooltip can say why. Enabled state and tooltip are
 * worked out when the toolbar {@linkplain #refresh refreshes} (give it triggers for whatever they
 * depend on), not every frame.
 */
public final class MachineToolbarButton implements ToolbarButton {

    private static final int ICON_SIZE = 8;
    private static final int DISABLED_TINT = ARGB.color(90, 0xFFFFFF);

    private final Identifier icon;
    private final Function<MachineUIMenu, Component> tooltip;
    private final Predicate<MachineUIMenu> enabled;
    private final BiPredicate<ToolbarContext, MachineUIMenu> active;
    private final BiConsumer<ToolbarContext, MachineUIMenu> action;

    /** Enabled state and tooltip, per toolbar context (one per open screen). */
    private record State(boolean enabled, Component tooltip) {
        static final State NONE = new State(false, Component.empty());
    }

    private final Map<ToolbarContext, State> states = new WeakHashMap<>();

    public MachineToolbarButton(Identifier icon, Function<MachineUIMenu, Component> tooltip, Predicate<MachineUIMenu> enabled,
                                BiPredicate<ToolbarContext, MachineUIMenu> active, BiConsumer<ToolbarContext, MachineUIMenu> action) {
        this.icon = icon;
        this.tooltip = tooltip;
        this.enabled = enabled;
        this.active = active;
        this.action = action;
    }

    @Override
    public void refresh(ToolbarContext context) {
        states.put(context, MachineUI.currentMenu()
                .map(menu -> new State(enabled.test(menu), tooltip.apply(menu)))
                .orElse(State.NONE));
    }

    private State state(ToolbarContext context) {
        return states.getOrDefault(context, State.NONE);
    }

    @Override
    public Component tooltip(ToolbarContext context) {
        return state(context).tooltip();
    }

    @Override
    public boolean isActive(ToolbarContext context) {
        return state(context).enabled() && MachineUI.currentMenu().map(menu -> active.test(context, menu)).orElse(false);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, ScreenRectangle area, ToolbarContext context) {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon,
                area.left() + (area.width() - ICON_SIZE) / 2, area.top() + (area.height() - ICON_SIZE) / 2,
                ICON_SIZE, ICON_SIZE, state(context).enabled() ? -1 : DISABLED_TINT);
    }

    @Override
    public void onClick(ToolbarContext context, ScreenRectangle area) {
        if (state(context).enabled())
            MachineUI.currentMenu().ifPresent(menu -> action.accept(context, menu));
    }
}
