package dev.compactmods.machines.upgrades.client;

import dev.compactmods.machines.upgrades.debug.DebugStorages;
import dev.robotgryphon.screenlib.graph.schema.types.ItemHandlerAccess;
import dev.robotgryphon.screenlib.nodes.api.render.PropertyValueRenderer;
import dev.robotgryphon.screenlib.nodes.api.render.PropertyValueRenderers;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Draws an item handler value as an icon, whatever its kind: a block's storage as screenlib's block
 * preview, an {@linkplain DebugStorages.InfiniteItems infinite source} as its item with an infinity
 * sign over it, and a {@linkplain DebugStorages.VoidItems void} as a barrier.
 */
public final class StorageRenderer implements PropertyValueRenderer<ItemHandlerAccess> {

    private static final int ICON_SIZE = 12;
    private static final int HOVER_FILL = 0x30FFFFFF;
    private static final Component INFINITY = Component.literal("∞").withStyle(ChatFormatting.AQUA);

    private @Nullable ItemStack barrier;

    @Override
    public Class<ItemHandlerAccess> valueType() {
        return ItemHandlerAccess.class;
    }

    @Override
    public int width() {
        return ICON_SIZE + 2;
    }

    @Override
    public boolean render(GuiGraphicsExtractor graphics, ItemHandlerAccess value, ScreenRectangle area,
                          int mouseX, int mouseY, float alpha) {
        final ItemStack icon = icon(value);
        if (icon == null) {
            final var block = blockRenderer();
            return block != null && block.render(graphics, value, area, mouseX, mouseY, alpha);
        }

        if (area.containsPoint(mouseX, mouseY))
            graphics.fill(area.left(), area.top(), area.right(), area.bottom(), HOVER_FILL);

        final int x = area.left() + (area.width() - ICON_SIZE) / 2;
        final int y = area.top() + (area.height() - ICON_SIZE) / 2;
        final var pose = graphics.pose();
        pose.pushMatrix();
        pose.translate(x, y);
        pose.scale(ICON_SIZE / 16f, ICON_SIZE / 16f);
        graphics.item(icon, 0, 0);
        pose.popMatrix();

        if (value instanceof DebugStorages.InfiniteItems) {
            // Over the item, bottom-right like a stack count
            final var font = Minecraft.getInstance().font;
            graphics.nextStratum();
            graphics.text(font, INFINITY, x + ICON_SIZE - font.width(INFINITY) + 1, y + ICON_SIZE - font.lineHeight + 2, -1, true);
        }
        return true;
    }

    @Override
    public boolean tooltip(GuiGraphicsExtractor graphics, ItemHandlerAccess value, ScreenRectangle area,
                           int mouseX, int mouseY, int screenX, int screenY) {
        final List<Component> lines = switch (value) {
            case DebugStorages.InfiniteItems infinite -> List.of(
                    Component.literal("Infinite ").append(infinite.item().getHoverName()),
                    Component.literal("Debug storage").withStyle(ChatFormatting.DARK_GRAY));
            case DebugStorages.VoidItems ignored -> List.of(
                    Component.literal("Void"),
                    Component.literal("Debug storage: destroys everything put in").withStyle(ChatFormatting.DARK_GRAY));
            default -> null;
        };
        if (lines == null) {
            final var block = blockRenderer();
            return block != null && block.tooltip(graphics, value, area, mouseX, mouseY, screenX, screenY);
        }

        graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lines, screenX, screenY);
        return true;
    }

    /** {@return the icon for a debug storage}, or {@code null} for anything else */
    private @Nullable ItemStack icon(ItemHandlerAccess value) {
        return switch (value) {
            case DebugStorages.InfiniteItems infinite -> infinite.item().toStack();
            case DebugStorages.VoidItems ignored -> {
                if (barrier == null)
                    barrier = new ItemStack(Items.BARRIER);
                yield barrier;
            }
            default -> null;
        };
    }

    /** Screenlib's block preview renderer, for block storages. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static @Nullable PropertyValueRenderer<ItemHandlerAccess> blockRenderer() {
        final var registry = (Registry<PropertyValueRenderer<?>>) BuiltInRegistries.REGISTRY
                .getValue((ResourceKey) PropertyValueRenderers.REGISTRY_KEY);
        if (registry == null)
            return null;
        return (PropertyValueRenderer<ItemHandlerAccess>) registry.getValue(PropertyValueRenderers.ITEM_HANDLER_BLOCK);
    }
}
