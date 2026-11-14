package dev.compactmods.machines.machine.client;

import dev.robotgryphon.screenlib.ui.api.panel.PanelType;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarButton;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

/**
 * A toolbar button toggling {@code panel}, drawn as an item's icon. The stack is made on first draw:
 * buttons are created while registries load, before items can make stacks.
 */
public final class ItemToolbarButton implements ToolbarButton {

    private static final int ICON_SIZE = 8;

    private final ItemLike item;
    private @Nullable ItemStack icon;
    private final Component tooltip;
    private final ResourceKey<PanelType<?>> panel;

    public ItemToolbarButton(ItemLike item, Component tooltip, ResourceKey<PanelType<?>> panel) {
        this.item = item;
        this.tooltip = tooltip;
        this.panel = panel;
    }

    @Override
    public Component tooltip(ToolbarContext context) {
        return tooltip;
    }

    @Override
    public boolean isActive(ToolbarContext context) {
        return context.isPanelOpen(panel);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, ScreenRectangle area, ToolbarContext context) {
        if (icon == null)
            icon = new ItemStack(item);

        final var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(area.left() + (area.width() - ICON_SIZE) / 2f, area.top() + (area.height() - ICON_SIZE) / 2f);
        pose.scale(ICON_SIZE / 16f, ICON_SIZE / 16f);
        graphics.item(icon, 0, 0);
        pose.popMatrix();
    }

    @Override
    public void onClick(ToolbarContext context, ScreenRectangle area) {
        context.togglePanel(panel);
    }
}
