package dev.compactmods.machines.room.preview.client.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/** Entry points for drawing room previews in GUIs. */
public final class RoomPreviewGui {

    private RoomPreviewGui() {}

    /** Mod-bus listener: registers {@link RoomPreviewGuiRenderer} for {@link RoomPreviewGuiState}. */
    public static void registerRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(RoomPreviewGuiState.class, RoomPreviewGuiRenderer::new);
    }

    /**
     * Queues a preview of {@code roomCode} filling {@code area} (in the graphics' current pose).
     * Draws nothing until the room's snapshot has arrived.
     */
    public static void submit(GuiGraphicsExtractor graphics, ScreenRectangle area, String roomCode,
                              Vector3fc camera, float yaw, float pitch, @Nullable Object hovered) {
        // PiP textures are axis-aligned in absolute GUI space: resolve the pose up front
        final Matrix3x2f pose = new Matrix3x2f(graphics.pose());
        final Vector2f topLeft = pose.transformPosition(area.left(), area.top(), new Vector2f());
        final Vector2f bottomRight = pose.transformPosition(area.right(), area.bottom(), new Vector2f());
        final int x0 = (int) Math.floor(topLeft.x());
        final int y0 = (int) Math.floor(topLeft.y());
        final int x1 = (int) Math.ceil(bottomRight.x());
        final int y1 = (int) Math.ceil(bottomRight.y());
        if (x1 <= x0 || y1 <= y0)
            return;

        final var scissor = graphics.peekScissorStack();
        graphics.submitPictureInPictureRenderState(new RoomPreviewGuiState(roomCode,
                camera.x(), camera.y(), camera.z(), yaw, pitch, hovered,
                x0, y0, x1, y1, scissor,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissor)));
    }
}
