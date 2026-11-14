package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.i18n.MachineTranslations;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.robotgryphon.screenlib.ui.api.panel.Panel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows what the machine holds: its core (drawn large on the left), and beside it the room it's bound
 * to or the template it will create. The panel sizes itself to its text, growing/shrinking around its
 * centre as the core changes.
 */
public class MachineInfoPanel extends Panel {

    private static final int LINE = 12;
    private static final int ITEM_SCALE = 3;
    private static final int ITEM_SIZE = 16 * ITEM_SCALE;
    /** Gap between the core item and the text. */
    private static final int GAP = 6;
    private static final int MIN_TEXT_WIDTH = 100;

    private record Line(Component text, int color) {}

    private final MachineUIMenu menu;

    public MachineInfoPanel(MachineUIMenu menu) {
        super(ITEM_SIZE + GAP + MIN_TEXT_WIDTH + 2 * PADDING, HEADER + ITEM_SIZE + 2 + PADDING, Machines.Blocks.MACHINE.get().getName());
        this.menu = menu;

        closable(false);
        draggable(true);
        open();
    }

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, ScreenRectangle content, int mouseX, int mouseY) {
        final var font = Minecraft.getInstance().font;
        final ItemStack core = MachineUI.core(menu);
        final List<Line> lines = lines(core);

        // The core's slot contents can arrive after the screen opens, so fit every frame
        if (fit(font, lines))
            content = contentArea();

        final int top = content.top() + 1;
        final int textX = content.left() + ITEM_SIZE + GAP;

        if (!core.isEmpty()) {
            final var pose = graphics.pose();
            pose.pushMatrix();
            pose.translate(content.left(), top);
            pose.scale(ITEM_SCALE, ITEM_SCALE);
            graphics.item(core, 0, 0);
            pose.popMatrix();
        }

        // Centre the text block against the item
        int y = top + Math.max(0, (ITEM_SIZE - lines.size() * LINE) / 2) + 2;
        for (Line line : lines) {
            graphics.text(font, line.text(), textX, y, line.color(), false);
            y += LINE;
        }
    }

    private List<Line> lines(ItemStack core) {
        final List<Line> lines = new ArrayList<>();
        if (core.isEmpty()) {
            lines.add(new Line(Component.literal("No room core"), CommonColors.LIGHT_GRAY));
            return lines;
        }

        lines.add(new Line(core.getOrDefault(DataComponents.CUSTOM_NAME, core.getItemName()), CommonColors.YELLOW));

        final var boundRoom = menu.currentRoom();
        if (boundRoom.isPresent()) {
            lines.add(new Line(Component.translatable(MachineTranslations.IDs.BOUND_TO, boundRoom.get()), CommonColors.WHITE));
            return lines;
        }

        menu.currentTemplate().ifPresent(template -> {
            final var key = Minecraft.getInstance().level.registryAccess()
                    .lookupOrThrow(RoomTemplate.REGISTRY_KEY)
                    .getKey(template);

            lines.add(new Line(Component.literal("Template: " + key), CommonColors.WHITE));
            lines.add(new Line(Component.translatable(MachineTranslations.IDs.SIZE, template.internalDimensions().toString()), CommonColors.WHITE));
            lines.add(new Line(Component.literal("Room pending creation"), CommonColors.LIGHT_GRAY));
        });
        return lines;
    }

    /** Resizes the panel to fit the item, text and title, keeping its centre. {@return whether it changed} */
    private boolean fit(Font font, List<Line> lines) {
        int textWidth = MIN_TEXT_WIDTH;
        for (Line line : lines)
            textWidth = Math.max(textWidth, font.width(line.text()));

        final int width = Math.max(ITEM_SIZE + GAP + textWidth, font.width(getMessage())) + 2 * PADDING;
        final int height = HEADER + Math.max(ITEM_SIZE + 2, lines.size() * LINE + 4) + PADDING;
        if (width == getWidth() && height == getHeight())
            return false;

        setRectangle(width, height, getX() - (width - getWidth()) / 2, getY() - (height - getHeight()) / 2);
        return true;
    }
}
