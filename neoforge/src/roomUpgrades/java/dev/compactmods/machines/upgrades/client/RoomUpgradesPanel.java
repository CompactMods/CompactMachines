package dev.compactmods.machines.upgrades.client;

import dev.compactmods.machines.room.CMFeatureFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import dev.robotgryphon.screenlib.ui.api.panel.Panel;

import java.util.List;

/**
 * The machine screen's room upgrades panel (toggled from its toolbar): lists the upgrades enabled on
 * the machine's room, fetched from the server each time the screen opens. Read-only for now;
 * upgrades are toggled with {@code /compactmachines upgrades}.
 */
public class RoomUpgradesPanel extends Panel {

    private static final int LINE = 12;

    private final String roomCode;
    private final boolean featureEnabled;
    private int scroll;

    public RoomUpgradesPanel(String roomCode) {
        super(160, 120, Component.literal("Room Upgrades"));
        this.roomCode = roomCode;

        final var level = Minecraft.getInstance().level;
        this.featureEnabled = level != null && CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(level.enabledFeatures());

        closable(false);
        draggable(true);
        resizable(120, 60);

        if (featureEnabled)
            ClientRoomUpgrades.request(roomCode);
    }

    public String roomCode() {
        return roomCode;
    }

    /** An upgrade's display name, from {@code room_upgrade.<namespace>.<path>}; falls back to its id. */
    public static Component name(Identifier upgrade) {
        return Component.translatableWithFallback(upgrade.toLanguageKey("room_upgrade"), upgrade.toString());
    }

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, ScreenRectangle content, int mouseX, int mouseY) {
        if (!featureEnabled) {
            message(graphics, content, Component.literal("Room upgrades are not enabled in this world."));
            return;
        }

        final var upgrades = ClientRoomUpgrades.get(roomCode);
        if (upgrades.isEmpty()) {
            message(graphics, content, Component.literal("Loading..."));
            return;
        }

        final List<Identifier> list = upgrades.get();
        if (list.isEmpty()) {
            message(graphics, content, Component.literal("No upgrades enabled."));
            return;
        }

        final var font = Minecraft.getInstance().font;
        scroll = Math.clamp(scroll, 0, maxScroll(list.size(), content));

        graphics.enableScissor(content.left(), content.top(), content.right(), content.bottom());
        int y = content.top() + 2 - scroll * LINE;
        for (Identifier upgrade : list) {
            graphics.text(font, name(upgrade), content.left(), y, CommonColors.WHITE, false);
            y += LINE;
        }
        graphics.disableScissor();
    }

    @Override
    protected boolean contentScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY == 0)
            return false;
        scroll -= (int) Math.signum(scrollY);
        return true;
    }

    private static int maxScroll(int lines, ScreenRectangle content) {
        return Math.max(0, lines - (content.height() - 2) / LINE);
    }

    private static void message(GuiGraphicsExtractor graphics, ScreenRectangle content, Component text) {
        graphics.text(Minecraft.getInstance().font, text, content.left(), content.top() + 4, CommonColors.LIGHT_GRAY, false);
    }
}
