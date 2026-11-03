package dev.compactmods.machines.room.preview.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Component;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Extra things drawn in the room preview screen by systems the preview doesn't know about (tunnels,
 * for example), usually fed by a server-side {@code RoomPreviewContributor}. Everything is in
 * room-local space: the interior spans {@code [0, size)} on each axis, so the walls sit at {@code -1}
 * and {@code size}.
 *
 * <p>Register with {@link #register}. Overlays can be hovered: the preview casts a ray from the mouse,
 * shows the nearest {@link Hit}'s tooltip and passes its {@link Hit#id() id} back to {@link #submit}.
 */
public interface RoomPreviewOverlay {

    /** Something under the mouse: {@code distance} along the ray, in blocks. */
    record Hit(Object id, float distance, List<Component> tooltip) {}

    /**
     * Submits this overlay's geometry for {@code roomCode}; {@code pose} maps room-local blocks to the
     * target space. {@code hovered} is the id of the hit under the mouse, from any overlay, or null.
     */
    void submit(String roomCode, PoseStack pose, SubmitNodeCollector collector, @Nullable Object hovered, float partialTick);

    /** {@return the nearest thing on the ray}, room-local, with {@code direction} normalised */
    Optional<Hit> pick(String roomCode, Vector3fc origin, Vector3fc direction);

    /** Drops all client-side data, on disconnect. */
    void clear();

    List<RoomPreviewOverlay> OVERLAYS = new CopyOnWriteArrayList<>();

    static void register(RoomPreviewOverlay overlay) {
        OVERLAYS.add(overlay);
    }

    static List<RoomPreviewOverlay> all() {
        return OVERLAYS;
    }

    /** {@return the nearest hit of any overlay} */
    static Optional<Hit> pickAll(String roomCode, Vector3fc origin, Vector3fc direction) {
        Hit best = null;
        for (RoomPreviewOverlay overlay : OVERLAYS) {
            final var hit = overlay.pick(roomCode, origin, direction).orElse(null);
            if (hit != null && (best == null || hit.distance() < best.distance()))
                best = hit;
        }
        return Optional.ofNullable(best);
    }
}
