package dev.compactmods.machines.room.preview.client.gui;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jspecify.annotations.Nullable;

/**
 * GUI render state for one room preview, drawn by {@link RoomPreviewGuiRenderer}. Coordinates are
 * absolute GUI pixels (any pose has already been applied by {@link RoomPreviewGui#submit}).
 *
 * @param camX  camera position, room-local blocks (see {@link PreviewCamera})
 * @param yaw   camera rotation around the vertical axis, radians
 * @param pitch camera tilt, radians (positive looks down)
 * @param hovered the {@link dev.compactmods.machines.room.preview.client.RoomPreviewOverlay.Hit#id() overlay hit} under the mouse, or null
 */
public record RoomPreviewGuiState(String roomCode, float camX, float camY, float camZ, float yaw, float pitch,
                                  @Nullable Object hovered,
                                  int x0, int y0, int x1, int y1,
                                  @Nullable ScreenRectangle scissorArea,
                                  @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState {

    @Override
    public float scale() {
        return 1f;
    }
}
