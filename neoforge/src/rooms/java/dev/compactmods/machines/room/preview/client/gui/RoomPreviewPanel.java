package dev.compactmods.machines.room.preview.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.room.client.RoomClientConfig;
import dev.compactmods.machines.room.preview.client.ClientRoomPreviews;
import dev.compactmods.machines.room.preview.client.RoomPreviewMesh;
import dev.compactmods.machines.room.preview.client.RoomPreviewMeshCache;
import dev.compactmods.machines.room.preview.client.RoomPreviewOverlay;
import dev.robotgryphon.screenlib.ui.api.UiHost;
import dev.robotgryphon.screenlib.ui.api.panel.Panel;
import dev.robotgryphon.screenlib.ui.api.widget.ContextMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.Optional;

/**
 * A screenlib panel showing a live 3D preview of a room, inspectable from any angle with a free
 * camera (see {@link PreviewCamera}): drag to look around, scroll to move forward/back along the
 * view, movement keys to fly. The controls live in the header's help icon tooltip; "Reset view" is
 * in the right-click menu, along with "Take screenshot". Header buttons turn the camera (without moving it) to face the room's
 * centre or the player's spawn; the machine's core shows as a slot in the top-left. While open it
 * keeps the room subscribed (see {@link ClientRoomPreviews#watch}), so the preview arrives even if no
 * machine bound to the room is in view.
 */
public class RoomPreviewPanel extends Panel {

    /** Longest frame step applied to key movement, so a hitch doesn't fling the camera. */
    private static final float MAX_STEP_SECONDS = 0.1f;

    private final UiHost host;
    private final String roomCode;
    private final Supplier<ItemStack> core;
    private final boolean enabled;
    private final PreviewCamera camera = new PreviewCamera();
    private long lastFrameNanos = -1;

    /** @param core the machine's current room core, shown in the panel's corner */
    public RoomPreviewPanel(UiHost host, String roomCode, Supplier<ItemStack> core) {
        super(220, 200, Component.literal(roomCode));
        this.host = host;
        this.roomCode = roomCode;
        this.core = core;
        this.enabled = RoomClientConfig.ENABLE_ROOM_PREVIEWS.get();

        closable(false);
        draggable(true);
        resizable(120, 100);
        open();

        if (enabled)
            ClientRoomPreviews.watch(roomCode);
    }

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, ScreenRectangle content, int mouseX, int mouseY) {
        if (!enabled) {
            centeredMessage(graphics, content, Component.translatableWithFallback(
                    CompactMachinesCore.dotPrefix("preview.disabled"), "Room Preview Disabled"));
            return;
        }

        final RoomPreviewMesh mesh = RoomPreviewMeshCache.get(roomCode);
        if (mesh == null) {
            centeredMessage(graphics, content, Component.translatableWithFallback(
                    CompactMachinesCore.dotPrefix("preview.loading"), "Loading room preview..."));
            return;
        }

        applyKeyMovement(mesh);

        final Vector3f position = camera.position(mesh.sizeX, mesh.sizeY, mesh.sizeZ);
        final var hit = pick(content, position, mouseX, mouseY);
        RoomPreviewGui.submit(graphics, content, roomCode, position, camera.yaw(), camera.pitch(),
                hit.map(RoomPreviewOverlay.Hit::id).orElse(null));

        // Overlays on top of the 3D preview
        graphics.nextStratum();
        hit.ifPresent(h -> graphics.setTooltipForNextFrame(Minecraft.getInstance().font, h.tooltip(), Optional.empty(), mouseX, mouseY));
        renderCoreSlot(graphics, content, mouseX, mouseY);
        headerButton(graphics, headerSlot(1), "?", true, mouseX, mouseY, controlsHelp());
        headerButton(graphics, headerSlot(2), "\u25CE", true, mouseX, mouseY,
                List.of(Component.literal("Look at room center")));
        final boolean hasSpawn = ClientRoomPreviews.spawn(roomCode) != null;
        headerButton(graphics, headerSlot(3), "\u2302", hasSpawn, mouseX, mouseY,
                List.of(Component.literal(hasSpawn ? "Look at your spawn" : "Your spawn isn't known yet")));
    }

    /**
     * {@return the overlay marker under the mouse}, if it's over the preview itself (not the core slot,
     * and the header buttons are outside the content area anyway).
     */
    private Optional<RoomPreviewOverlay.Hit> pick(ScreenRectangle content, Vector3f position, int mouseX, int mouseY) {
        if (!content.containsPoint(mouseX, mouseY) || content.width() <= 0 || content.height() <= 0)
            return Optional.empty();
        if (!core.get().isEmpty() && coreSlot(content).containsPoint(mouseX, mouseY))
            return Optional.empty();

        // Pixel centres, so the ray matches what's drawn under the cursor
        final float ndcX = 2f * (mouseX + 0.5f - content.left()) / content.width() - 1f;
        final float ndcY = 1f - 2f * (mouseY + 0.5f - content.top()) / content.height();
        final Vector3f direction = PreviewCamera.ray(ndcX, ndcY, (float) content.width() / content.height(),
                camera.yaw(), camera.pitch());
        return RoomPreviewOverlay.pickAll(roomCode, position, direction);
    }

    private static ScreenRectangle coreSlot(ScreenRectangle content) {
        return new ScreenRectangle(content.left() + 2, content.top() + 2, 18, 18);
    }

    /** The machine's core as an item slot in the preview's top-left corner; hover for its tooltip. */
    private void renderCoreSlot(GuiGraphicsExtractor graphics, ScreenRectangle content, int mouseX, int mouseY) {
        final ItemStack stack = core.get();
        if (stack.isEmpty())
            return;

        final var slot = coreSlot(content);
        final int x = slot.left(), y = slot.top();
        graphics.fill(x, y, x + 18, y + 18, 0xA0000000);
        graphics.outline(x, y, 18, 18, 0x60FFFFFF);
        graphics.item(stack, x + 1, y + 1);

        if (mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18)
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font, stack, mouseX, mouseY);
    }

    /**
     * Draws a header glyph button (the header's right side is free: the panel isn't closable);
     * hover shows {@code tooltip}.
     */
    private static void headerButton(GuiGraphicsExtractor graphics, ScreenRectangle area, String glyph, boolean active,
                                     int mouseX, int mouseY, List<Component> tooltip) {
        final var font = Minecraft.getInstance().font;
        final boolean hovered = area.containsPoint(mouseX, mouseY);
        final int color = !active ? 0xFF555555 : hovered ? 0xFFFFFFFF : 0xFFA0A0A0;
        graphics.centeredText(font, Component.literal(glyph), area.left() + area.width() / 2,
                area.top() + (area.height() - font.lineHeight) / 2 + 1, color);

        if (hovered)
            graphics.setTooltipForNextFrame(font, tooltip, Optional.empty(), mouseX, mouseY);
    }

    /** The {@code n}th header button slot from the right (1 = rightmost). */
    private ScreenRectangle headerSlot(int n) {
        return new ScreenRectangle(getRight() - n * HEADER, getY(), HEADER, HEADER);
    }

    /** Header buttons sit on the panel's drag handle, so they're checked before the panel's own handling. */
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        final var mesh = mesh();
        if (isOpen() && mesh != null && event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
            final int x = (int) event.x(), y = (int) event.y();
            if (headerSlot(2).containsPoint(x, y)) {
                camera.lookAtCenter(mesh.sizeX, mesh.sizeY, mesh.sizeZ);
                return true;
            }
            if (headerSlot(3).containsPoint(x, y)) {
                final var spawn = ClientRoomPreviews.spawn(roomCode);
                // Spawns are feet positions; aim at roughly where a player's body would be
                if (spawn != null)
                    camera.lookAt(new Vector3f(spawn.position()).add(0f, 1f, 0f), mesh.sizeX, mesh.sizeY, mesh.sizeZ);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private static List<Component> controlsHelp() {
        final var options = Minecraft.getInstance().options;
        final Component movement = Component.empty()
                .append(options.keyUp.getTranslatedKeyMessage()).append(Component.literal("/"))
                .append(options.keyLeft.getTranslatedKeyMessage()).append(Component.literal("/"))
                .append(options.keyDown.getTranslatedKeyMessage()).append(Component.literal("/"))
                .append(options.keyRight.getTranslatedKeyMessage());

        return List.of(
                Component.literal("Room preview").withStyle(ChatFormatting.YELLOW),
                Component.literal("Drag: look around"),
                Component.literal("Scroll: move forward/back"),
                Component.empty().append(movement).append(Component.literal(": move")),
                Component.empty().append(options.keyJump.getTranslatedKeyMessage()).append(Component.literal("/"))
                        .append(options.keyShift.getTranslatedKeyMessage()).append(Component.literal(": up/down")),
                Component.literal("Right-click: screenshot, reset view").withStyle(ChatFormatting.GRAY));
    }

    /** Polls the movement keys (they don't reach key mappings while a screen is open). */
    private void applyKeyMovement(RoomPreviewMesh mesh) {
        final long now = Util.getNanos();
        final float seconds = lastFrameNanos < 0 ? 0f : Math.min(MAX_STEP_SECONDS, (now - lastFrameNanos) / 1.0e9f);
        lastFrameNanos = now;

        if (!isFocused())
            return;

        final var options = Minecraft.getInstance().options;
        final int forward = held(options.keyUp) - held(options.keyDown);
        final int strafe = held(options.keyRight) - held(options.keyLeft);
        final int up = held(options.keyJump) - held(options.keyShift);
        camera.move(forward, strafe, up, seconds, mesh.sizeX, mesh.sizeY, mesh.sizeZ);
    }

    private static int held(KeyMapping mapping) {
        final var key = mapping.getKey();
        return key.getType() == InputConstants.Type.KEYBOARD && InputConstants.isKeyDown(key.getValue()) ? 1 : 0;
    }

    private @Nullable RoomPreviewMesh mesh() {
        return RoomPreviewMeshCache.get(roomCode);
    }

    private static void centeredMessage(GuiGraphicsExtractor graphics, ScreenRectangle content, Component message) {
        final var font = Minecraft.getInstance().font;
        graphics.centeredText(font, message, content.left() + content.width() / 2,
                content.top() + (content.height() - font.lineHeight) / 2, 0xFFDEDEDE);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Swallow movement keys while focused, so they don't trigger anything else on the screen
        return isFocused() && isMovementKey(event.key());
    }

    private static boolean isMovementKey(int key) {
        final var options = Minecraft.getInstance().options;
        return List.of(options.keyUp, options.keyDown, options.keyLeft, options.keyRight, options.keyJump, options.keyShift)
                .stream()
                .anyMatch(mapping -> mapping.getKey().getType() == InputConstants.Type.KEYBOARD && mapping.getKey().getValue() == key);
    }

    @Override
    protected boolean contentClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT) {
            host.openMenu(new ContextMenu((int) event.x(), (int) event.y(), List.of(
                    new ContextMenu.Item(Component.literal("Take screenshot"), () -> RoomPreviewGuiRenderer.requestScreenshot(roomCode)),
                    new ContextMenu.Item(Component.literal("Reset view"), camera::reset))));
        }
        return true;
    }

    @Override
    protected boolean contentDragged(MouseButtonEvent event, double dx, double dy) {
        camera.look(dx, dy);
        return true;
    }

    @Override
    protected boolean contentScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        final var mesh = mesh();
        if (mesh != null)
            camera.dolly(scrollY, mesh.sizeX, mesh.sizeY, mesh.sizeZ);
        return true;
    }

    @Override
    public void removed() {
        super.removed();
        ClientRoomPreviews.unwatch(roomCode);
    }
}
