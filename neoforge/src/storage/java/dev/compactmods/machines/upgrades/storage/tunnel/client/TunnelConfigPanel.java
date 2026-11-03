package dev.compactmods.machines.upgrades.storage.tunnel.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.compactmods.machines.upgrades.storage.tunnel.NamedStorageService;
import dev.compactmods.machines.upgrades.storage.tunnel.RoomStorageHandlers;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelTranslations;
import dev.compactmods.machines.upgrades.storage.tunnel.TunnelType;
import dev.compactmods.machines.upgrades.storage.tunnel.menu.TunnelConfigMenu;
import dev.compactmods.machines.upgrades.storage.tunnel.network.TunnelConfigSubmitPacket;
import dev.robotgryphon.screenlib.ui.api.panel.Panel;
import dev.robotgryphon.screenlib.ui.api.toolbar.ToolbarStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.ContainerInput;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The tunnel config screen's panel: a name field, a grid of the available tunnel types, the
 * container slot and a create/save button. In edit mode the tunnel type is locked to the bound
 * storage's.
 */
public class TunnelConfigPanel extends Panel {

    private static final int SLOT = 18;
    private static final int WIDTH = 9 * SLOT + 2 * PADDING;
    private static final int CELL = 22;
    private static final int ICON = 16;
    private static final int FIELD_HEIGHT = 14;
    private static final int BUTTON_HEIGHT = 16;
    private static final int GAP = 4;
    private static final int ERROR_LINES = 2;

    private static final int SLOT_COLOR = 0x40FFFFFF;
    private static final int HOVER_COLOR = 0x80FFFFFF;
    private static final int SELECTED_OUTLINE = 0xFFFFFFFF;
    private static final int LOCKED_TINT = ARGB.color(90, 0xFFFFFF);
    private static final int ERROR_COLOR = 0xFFFF6B6B;

    private final TunnelConfigMenu menu;
    private final List<Holder.Reference<TunnelType>> types;
    private final EditBox name;

    private @Nullable ResourceKey<TunnelType> selected;
    private int seenRevision = -1;

    public TunnelConfigPanel(TunnelConfigMenu menu) {
        super(WIDTH, height(availableTypes().size()), TunnelTranslations.SCREEN_TITLE.get());
        this.menu = menu;
        this.types = availableTypes();

        final var font = Minecraft.getInstance().font;
        this.name = new EditBox(font, 0, 0, WIDTH - 2 * PADDING, FIELD_HEIGHT, TunnelTranslations.NAME.get());
        this.name.setMaxLength(NamedStorageService.MAX_NAME_LENGTH);

        this.closable(false);
        this.draggable(true);
        this.open();
        this.refresh();
    }

    /** Tunnel types a storage can be created with, from the synced registry. */
    private static List<Holder.Reference<TunnelType>> availableTypes() {
        final var level = Minecraft.getInstance().level;
        if (level == null)
            return List.of();

        return level.registryAccess().lookupOrThrow(TunnelType.REGISTRY_KEY).listElements()
                .filter(type -> RoomStorageHandlers.supports(type.value().resourceType()))
                .sorted(Comparator.comparing(type -> type.key().identifier().toString()))
                .toList();
    }

    private static int height(int typeCount) {
        final int rows = Math.max(1, Mth.positiveCeilDiv(typeCount, columns()));
        final var font = Minecraft.getInstance().font;
        return HEADER + font.lineHeight + 1 + FIELD_HEIGHT + GAP
                + rows * CELL + GAP
                + SLOT + GAP
                + BUTTON_HEIGHT + GAP
                + ERROR_LINES * font.lineHeight + PADDING;
    }

    private static int columns() {
        return (WIDTH - 2 * PADDING) / CELL;
    }

    private boolean editing() {
        return menu.state().isPresent();
    }

    /** Picks up a new menu state (e.g. the switch to edit mode after creating the storage). */
    private void refresh() {
        if (seenRevision == menu.revision())
            return;

        seenRevision = menu.revision();
        menu.state().ifPresent(state -> {
            if (!name.getValue().equals(state.storage().name()))
                name.setValue(state.storage().name());
            selected = state.storage().type();
        });
    }

    private Optional<TunnelType> selectedType() {
        return types.stream()
                .filter(type -> type.key().equals(selected))
                .findFirst()
                .map(Holder.Reference::value);
    }

    // region Layout

    private ScreenRectangle nameArea(ScreenRectangle content) {
        final var font = Minecraft.getInstance().font;
        return new ScreenRectangle(content.left(), content.top() + font.lineHeight + 1, content.width(), FIELD_HEIGHT);
    }

    private ScreenRectangle cellArea(ScreenRectangle content, int index) {
        final int top = nameArea(content).bottom() + GAP;
        final int cols = columns();
        return new ScreenRectangle(content.left() + (index % cols) * CELL, top + (index / cols) * CELL, CELL, CELL);
    }

    private int gridBottom(ScreenRectangle content) {
        final int rows = Math.max(1, Mth.positiveCeilDiv(types.size(), columns()));
        return nameArea(content).bottom() + GAP + rows * CELL;
    }

    private ScreenRectangle slotArea(ScreenRectangle content) {
        return new ScreenRectangle(content.left(), gridBottom(content) + GAP, SLOT, SLOT);
    }

    private ScreenRectangle buttonArea(ScreenRectangle content) {
        return new ScreenRectangle(content.left(), slotArea(content).bottom() + GAP, content.width(), BUTTON_HEIGHT);
    }

    // endregion

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, ScreenRectangle content, int mouseX, int mouseY) {
        refresh();
        final var font = Minecraft.getInstance().font;

        // Name
        graphics.text(font, TunnelTranslations.NAME.get(), content.left(), content.top(), ToolbarStyle.TEXT, false);
        final var nameArea = nameArea(content);
        name.setPosition(nameArea.left(), nameArea.top());
        name.extractRenderState(graphics, mouseX, mouseY, 0);

        // Tunnel types
        for (int i = 0; i < types.size(); i++) {
            final var type = types.get(i);
            final var cell = cellArea(content, i);
            final boolean isSelected = type.key().equals(selected);
            final boolean locked = editing() && !isSelected;
            final boolean hovered = cell.containsPoint(mouseX, mouseY);

            ToolbarStyle.buttonBackground(graphics, cell, hovered && !locked, isSelected);
            if (isSelected)
                graphics.outline(cell.left(), cell.top(), cell.width(), cell.height(), SELECTED_OUTLINE);

            final int iconX = cell.left() + (CELL - ICON) / 2;
            final int iconY = cell.top() + 1;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, type.value().icon(), iconX, iconY, ICON, ICON, locked ? LOCKED_TINT : -1);
            graphics.fill(iconX, iconY + ICON + 1, iconX + ICON, iconY + ICON + 3, ARGB.opaque(type.value().color()));

            if (hovered)
                graphics.setTooltipForNextFrame(typeName(type.key()), mouseX, mouseY);
        }

        renderSlot(graphics, font, content, mouseX, mouseY);

        // Create / save
        final var button = buttonArea(content);
        final boolean buttonHovered = button.containsPoint(mouseX, mouseY);
        ToolbarStyle.buttonBackground(graphics, button, buttonHovered, false);
        graphics.outline(button.left(), button.top(), button.width(), button.height(), ToolbarStyle.OUTLINE);
        final var label = editing() ? TunnelTranslations.SAVE.get() : TunnelTranslations.CREATE.get();
        graphics.centeredText(font, label, button.left() + button.width() / 2,
                button.top() + (BUTTON_HEIGHT - font.lineHeight) / 2 + 1, ToolbarStyle.TEXT);

        // Last error
        menu.error().ifPresent(error -> {
            int y = button.bottom() + GAP;
            for (var line : font.split(error, content.width()).stream().limit(ERROR_LINES).toList()) {
                graphics.text(font, line, content.left(), y, ERROR_COLOR, false);
                y += font.lineHeight;
            }
        });
    }

    private void renderSlot(GuiGraphicsExtractor graphics, Font font, ScreenRectangle content, int mouseX, int mouseY) {
        final var area = slotArea(content);
        final int x = area.left() + 1;
        final int y = area.top() + 1;
        graphics.fill(x, y, x + 16, y + 16, SLOT_COLOR);

        final var stack = menu.containerSlot().getItem();
        if (!stack.isEmpty()) {
            graphics.item(stack, x, y);
            graphics.itemDecorations(font, stack, x, y);
        }

        final boolean hovered = area.containsPoint(mouseX, mouseY);
        if (hovered) {
            graphics.fill(x, y, x + 16, y + 16, HOVER_COLOR);
            if (!stack.isEmpty() && menu.getCarried().isEmpty())
                graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
        }

        // Capacity (including what the slot would add) and which container adds to it
        selectedType().ifPresent(type -> {
            final var container = type.container().value();
            final int current = menu.state().map(s -> s.capacity()).orElse(0);
            final int capacity = current + type.capacity(menu.containerCount(container));
            final int textX = area.right() + GAP;
            final int textWidth = content.right() - textX;

            graphics.text(font, TunnelTranslations.CAPACITY.apply(capacity), textX, area.top(), ToolbarStyle.TEXT, false);
            final var hint = TunnelTranslations.CONTAINER_HINT.apply(container.getDefaultInstance().getHoverName());
            font.split(hint, textWidth).stream().findFirst()
                    .ifPresent(line -> graphics.text(font, line, textX, area.top() + font.lineHeight, ToolbarStyle.ICON, false));
        });
    }

    private static Component typeName(ResourceKey<TunnelType> key) {
        final var id = key.identifier();
        return Component.translatableWithFallback(TunnelType.translationKey(id), id.getPath());
    }

    @Override
    protected boolean contentClicked(MouseButtonEvent event, boolean doubleClick) {
        final var content = contentArea();
        final int mx = Mth.floor(event.x());
        final int my = Mth.floor(event.y());

        if (name.isMouseOver(event.x(), event.y())) {
            name.setFocused(true);
            return name.mouseClicked(event, doubleClick);
        }
        name.setFocused(false);

        if (!editing()) {
            for (int i = 0; i < types.size(); i++) {
                if (cellArea(content, i).containsPoint(mx, my)) {
                    selected = types.get(i).key();
                    return true;
                }
            }
        }

        if (slotArea(content).containsPoint(mx, my)) {
            final var mc = Minecraft.getInstance();
            if (mc.gameMode != null && mc.player != null) {
                final var input = event.hasShiftDown() ? ContainerInput.QUICK_MOVE : ContainerInput.PICKUP;
                mc.gameMode.handleContainerInput(menu.containerId, menu.containerSlot().index, event.button(), input, mc.player);
            }
            return true;
        }

        if (buttonArea(content).containsPoint(mx, my)) {
            submit();
            return true;
        }

        return true;
    }

    private void submit() {
        ClientPacketDistributor.sendToServer(new TunnelConfigSubmitPacket(name.getValue(), Optional.ofNullable(selected)));
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (name.isFocused() && (event.key() == InputConstants.KEY_RETURN || event.key() == InputConstants.KEY_NUMPADENTER)) {
            submit();
            return true;
        }
        return name.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return name.charTyped(event);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (!focused)
            name.setFocused(false);
    }
}
