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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * An icon button for the machine screen's toolbar, drawn as a GUI sprite or an item's icon, bound to the open {@link MachineUIMenu} (see
 * {@link MachineUI#currentMenu()}). Unlike screenlib's {@code IconToolbarButton} it can be disabled:
 * it then draws dimmed, ignores clicks, and its tooltip can say why. Enabled state and tooltip are
 * worked out when the toolbar {@linkplain #refresh refreshes} (give it triggers for whatever they
 * depend on), not every frame.
 */
public final class MachineToolbarButton implements ToolbarButton {

    private static final int ICON_SIZE = 8;
    private static final int DISABLED_TINT = ARGB.color(90, 0xFFFFFF);
    private static final int DISABLED_ITEM_SHADE = ARGB.color(160, 0x202020);

    /** A GUI sprite, or null for {@link #item}. */
    private final @Nullable Identifier icon;
    private final @Nullable Supplier<? extends ItemLike> item;
    /** Made on first draw: buttons are created while registries load, before items can make stacks. */
    private @Nullable ItemStack itemIcon;
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
        this(icon, null, tooltip, enabled, active, action);
    }

    /** A button drawn as {@code item}'s icon. */
    public static MachineToolbarButton item(Supplier<? extends ItemLike> item, Function<MachineUIMenu, Component> tooltip,
                                            Predicate<MachineUIMenu> enabled, BiPredicate<ToolbarContext, MachineUIMenu> active,
                                            BiConsumer<ToolbarContext, MachineUIMenu> action) {
        return new MachineToolbarButton(null, item, tooltip, enabled, active, action);
    }

    private MachineToolbarButton(@Nullable Identifier icon, @Nullable Supplier<? extends ItemLike> item,
                                 Function<MachineUIMenu, Component> tooltip, Predicate<MachineUIMenu> enabled,
                                 BiPredicate<ToolbarContext, MachineUIMenu> active, BiConsumer<ToolbarContext, MachineUIMenu> action) {
        this.icon = icon;
        this.item = item;
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
        final int x = area.left() + (area.width() - ICON_SIZE) / 2, y = area.top() + (area.height() - ICON_SIZE) / 2;
        final boolean enabled = state(context).enabled();
        if (icon != null) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, x, y, ICON_SIZE, ICON_SIZE, enabled ? -1 : DISABLED_TINT);
            return;
        }

        if (itemIcon == null && item != null)
            itemIcon = new ItemStack(item.get());

        final var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(ICON_SIZE / 16f, ICON_SIZE / 16f);
        graphics.item(itemIcon, 0, 0);
        pose.popMatrix();
        // Items can't be tinted like sprites; dim them instead
        if (!enabled)
            graphics.fill(x, y, x + ICON_SIZE, y + ICON_SIZE, DISABLED_ITEM_SHADE);
    }

    @Override
    public void onClick(ToolbarContext context, ScreenRectangle area) {
        if (state(context).enabled())
            MachineUI.currentMenu().ifPresent(menu -> action.accept(context, menu));
    }
}
