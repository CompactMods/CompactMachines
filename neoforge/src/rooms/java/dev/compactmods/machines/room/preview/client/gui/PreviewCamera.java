package dev.compactmods.machines.room.preview.client.gui;

import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * The room preview's free camera, in room-local space (blocks; the room's interior spans
 * {@code [0, size)} on each axis). Spectator-style: it passes through blocks.
 * <ul>
 *     <li>{@link #look} turns the camera in place.</li>
 *     <li>{@link #dolly} moves it forward/back along its view direction, as if on a rail.</li>
 *     <li>{@link #move} flies it with the movement keys.</li>
 * </ul>
 * It starts (and {@link #reset resets}) outside the room, looking at its centre with the whole room
 * in frame. Step sizes scale with the room, so large and small rooms feel the same.
 *
 * <p>Movement is confined so the camera can't get lost: never below the room's floor, and otherwise
 * at most {@link #BOUNDS_BUFFER} blocks beyond the room or the default viewpoint, whichever is
 * farther out (the default view stays reachable).
 */
public final class PreviewCamera {

    static final float DEFAULT_YAW = Mth.PI / 4f;
    static final float DEFAULT_PITCH = Mth.PI / 6f;
    private static final float MAX_PITCH = Mth.HALF_PI - 0.01f;
    private static final float LOOK_RADIANS_PER_PIXEL = 0.01f;
    /** Scroll notches to cover the default viewing distance. */
    private static final float DOLLY_STEPS_PER_FRAMING = 24f;
    /** Seconds of key movement to cover the default viewing distance. */
    private static final float MOVE_SECONDS_PER_FRAMING = 5f;
    private static final float MIN_STEP = 0.25f;
    /** How far past the room (or the default viewpoint) the camera may go, except below the floor. */
    static final float BOUNDS_BUFFER = 4f;

    private float yaw = DEFAULT_YAW;
    private float pitch = DEFAULT_PITCH;
    /** {@code null} until framed for a room (on first use and after {@link #reset}). */
    private @Nullable Vector3f position;

    public float yaw() {
        return yaw;
    }

    public float pitch() {
        return pitch;
    }

    /** Back to the default view of the room. */
    public void reset() {
        yaw = DEFAULT_YAW;
        pitch = DEFAULT_PITCH;
        position = null;
    }

    /** {@return the camera position} for a room of the given interior size. */
    public Vector3f position(int sizeX, int sizeY, int sizeZ) {
        return new Vector3f(livePosition(sizeX, sizeY, sizeZ));
    }

    /** Mouse-drag look: turns the camera where it stands. */
    public void look(double dx, double dy) {
        yaw += (float) dx * LOOK_RADIANS_PER_PIXEL;
        pitch = Mth.clamp(pitch + (float) dy * LOOK_RADIANS_PER_PIXEL, -MAX_PITCH, MAX_PITCH);
    }

    /** Turns the camera, where it stands, to face {@code target} (room-local). */
    public void lookAt(Vector3fc target, int sizeX, int sizeY, int sizeZ) {
        final Vector3f dir = new Vector3f(target).sub(livePosition(sizeX, sizeY, sizeZ));
        if (dir.lengthSquared() < 1e-6f)
            return;

        // Inverse of backward(): the view direction is -backward(yaw, pitch)
        dir.normalize();
        pitch = Mth.clamp((float) Math.asin(-dir.y), -MAX_PITCH, MAX_PITCH);
        yaw = (float) Mth.atan2(dir.x, -dir.z);
    }

    /** Turns the camera, where it stands, to face the room's centre. */
    public void lookAtCenter(int sizeX, int sizeY, int sizeZ) {
        lookAt(new Vector3f(sizeX / 2f, sizeY / 2f, sizeZ / 2f), sizeX, sizeY, sizeZ);
    }

    /** Scroll: moves the camera forward ({@code amount > 0}) or back along where it's looking. */
    public void dolly(double amount, int sizeX, int sizeY, int sizeZ) {
        final float step = Math.max(MIN_STEP, RoomPreviewGuiRenderer.framingDistance(sizeX, sizeY, sizeZ) / DOLLY_STEPS_PER_FRAMING);
        livePosition(sizeX, sizeY, sizeZ).fma((float) -amount * step, backward(yaw, pitch));
        confine(sizeX, sizeY, sizeZ);
    }

    /**
     * Key movement for {@code seconds} of input. {@code forward}/{@code strafe}/{@code up} are -1, 0
     * or 1; forward and strafe stay level (like a spectator), up is world-vertical.
     */
    public void move(int forward, int strafe, int up, float seconds, int sizeX, int sizeY, int sizeZ) {
        if (forward == 0 && strafe == 0 && up == 0)
            return;

        final float speed = Math.max(MIN_STEP, RoomPreviewGuiRenderer.framingDistance(sizeX, sizeY, sizeZ) / MOVE_SECONDS_PER_FRAMING);
        final float sin = Mth.sin(yaw), cos = Mth.cos(yaw);
        final Vector3f motion = new Vector3f(
                forward * sin + strafe * cos,
                up,
                -forward * cos + strafe * sin);
        livePosition(sizeX, sizeY, sizeZ).fma(speed * seconds / Math.max(1f, motion.length()), motion);
        confine(sizeX, sizeY, sizeZ);
    }

    /** Clamps the camera into {@link #bounds}. */
    private void confine(int sizeX, int sizeY, int sizeZ) {
        final Vector3f[] box = bounds(sizeX, sizeY, sizeZ);
        livePosition(sizeX, sizeY, sizeZ).max(box[0]).min(box[1]);
    }

    /**
     * {@return the min and max corners the camera may move within} — the room and the default
     * viewpoint, grown by {@link #BOUNDS_BUFFER} on every side except below, where the room's floor
     * (room-local {@code y = 0}) is the limit.
     */
    static Vector3f[] bounds(int sizeX, int sizeY, int sizeZ) {
        final Vector3f home = defaultPosition(sizeX, sizeY, sizeZ);
        return new Vector3f[]{
                new Vector3f(Math.min(0, home.x) - BOUNDS_BUFFER, 0f, Math.min(0, home.z) - BOUNDS_BUFFER),
                new Vector3f(Math.max(sizeX, home.x) + BOUNDS_BUFFER, Math.max(sizeY, home.y) + BOUNDS_BUFFER,
                        Math.max(sizeZ, home.z) + BOUNDS_BUFFER)
        };
    }

    /** Where the camera starts: back from the room's centre, looking at it with the room in frame. */
    private static Vector3f defaultPosition(int sizeX, int sizeY, int sizeZ) {
        return new Vector3f(sizeX / 2f, sizeY / 2f, sizeZ / 2f)
                .fma(RoomPreviewGuiRenderer.framingDistance(sizeX, sizeY, sizeZ), backward(DEFAULT_YAW, DEFAULT_PITCH));
    }

    /** {@return the view matrix} for a camera at {@code position} with the given orientation. */
    public static Matrix4f view(Vector3fc position, float yaw, float pitch) {
        return new Matrix4f()
                .rotateX(pitch)
                .rotateY(yaw)
                .translate(-position.x(), -position.y(), -position.z());
    }

    /**
     * {@return the unit direction, room-local, of the ray through a point of the preview}
     *
     * @param ndcX   -1 (left edge) to 1 (right edge)
     * @param ndcY   -1 (bottom edge) to 1 (top edge)
     * @param aspect the preview's width over its height
     */
    public static Vector3f ray(float ndcX, float ndcY, float aspect, float yaw, float pitch) {
        final float tan = (float) Math.tan(RoomPreviewGuiRenderer.FOV_DEGREES * Mth.DEG_TO_RAD / 2f);
        final Vector3f dir = new Vector3f(ndcX * tan * aspect, ndcY * tan, -1f).normalize();
        // Camera space to room space: the inverse of view()'s rotation
        return new Matrix4f().rotateX(pitch).rotateY(yaw).transpose3x3().transformDirection(dir).normalize();
    }

    /** Unit vector pointing behind the camera (opposite to where it looks). */
    static Vector3f backward(float yaw, float pitch) {
        final float cosPitch = Mth.cos(pitch);
        return new Vector3f(-Mth.sin(yaw) * cosPitch, Mth.sin(pitch), Mth.cos(yaw) * cosPitch);
    }

    /** The live position, framing the room first if needed: back from its centre, looking at it. */
    private Vector3f livePosition(int sizeX, int sizeY, int sizeZ) {
        if (position == null)
            position = defaultPosition(sizeX, sizeY, sizeZ);
        return position;
    }
}
