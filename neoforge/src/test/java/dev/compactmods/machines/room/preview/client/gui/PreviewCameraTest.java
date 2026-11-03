package dev.compactmods.machines.room.preview.client.gui;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreviewCameraTest {

    // Room 102V-K3G3-TN39's interior
    private static final int SX = 21, SY = 11, SZ = 21;
    // Blocks; float trig at ~30 blocks from the centre is good to a few thousandths
    private static final float EPS = 1e-2f;

    private static Vector3f eye(PreviewCamera camera, Vector3f world) {
        return PreviewCamera.view(camera.position(SX, SY, SZ), camera.yaw(), camera.pitch())
                .transformPosition(new Vector3f(world));
    }

    private static Vector3f center() {
        return new Vector3f(SX / 2f, SY / 2f, SZ / 2f);
    }

    private static void assertStraightAhead(PreviewCamera camera, Vector3f world) {
        final Vector3f e = eye(camera, world);
        assertEquals(0f, e.x, EPS, "off-axis: " + e);
        assertEquals(0f, e.y, EPS, "off-axis: " + e);
        assertTrue(e.z < 0f, "should be in front of the camera: " + e);
    }

    @Test
    void startsOutsideTheRoomLookingAtItsCentre() {
        final var camera = new PreviewCamera();
        assertStraightAhead(camera, center());

        final Vector3f pos = camera.position(SX, SY, SZ);
        assertTrue(pos.x < 0 || pos.x > SX || pos.y < 0 || pos.y > SY || pos.z < 0 || pos.z > SZ,
                "default view should start outside the room: " + pos);
    }

    @Test
    void lookingAroundKeepsTheCameraInPlace() {
        final var camera = new PreviewCamera();
        final Vector3f before = camera.position(SX, SY, SZ);
        camera.look(137, -42);
        assertTrue(before.equals(camera.position(SX, SY, SZ), EPS));
    }

    @Test
    void lookAtTurnsInPlaceToFaceTheTarget() {
        final var camera = new PreviewCamera();
        camera.move(1, -1, 0, 1.5f, SX, SY, SZ);
        camera.look(240, -80);
        final Vector3f before = camera.position(SX, SY, SZ);

        // A spawn-like point near the floor, off to one side
        final Vector3f spawn = new Vector3f(3.5f, 1f, 17.5f);
        camera.lookAt(spawn, SX, SY, SZ);
        assertTrue(before.equals(camera.position(SX, SY, SZ), EPS), "lookAt must not move the camera");
        assertStraightAhead(camera, spawn);

        camera.lookAtCenter(SX, SY, SZ);
        assertTrue(before.equals(camera.position(SX, SY, SZ), EPS));
        assertStraightAhead(camera, center());
    }

    @Test
    void scrollingMovesAlongTheViewLikeARail() {
        final var camera = new PreviewCamera();
        camera.look(-60, 25);
        final Vector3f start = camera.position(SX, SY, SZ);

        camera.dolly(3, SX, SY, SZ);
        final Vector3f forward = camera.position(SX, SY, SZ);
        // The start is now straight behind the camera — it moved along its own view direction...
        final Vector3f behind = eye(camera, start);
        assertEquals(0f, behind.x, EPS);
        assertEquals(0f, behind.y, EPS);
        assertTrue(behind.z > 0f, "moved backwards: " + behind);

        // ...and scrolling back the same amount returns exactly to the start
        camera.dolly(-3, SX, SY, SZ);
        assertTrue(start.equals(camera.position(SX, SY, SZ), EPS), start + " vs " + camera.position(SX, SY, SZ));
        assertFalse(start.equals(forward, EPS));
    }

    @Test
    void movingForwardFollowsTheLevelViewDirection() {
        final var camera = new PreviewCamera();
        camera.look(-60, -camera.pitch() / 0.01); // level the camera (pitch 0) at some yaw
        final Vector3f before = camera.position(SX, SY, SZ);

        camera.move(1, 0, 0, 1f, SX, SY, SZ);
        final Vector3f old = eye(camera, before);
        assertEquals(0f, old.x, EPS);
        assertEquals(0f, old.y, EPS);
        assertTrue(old.z > 0f, "moved backwards: " + old);
    }

    @Test
    void cannotFlyOrScrollBelowTheFloor() {
        final var camera = new PreviewCamera();
        camera.move(0, 0, -1, 600f, SX, SY, SZ);
        assertEquals(0f, camera.position(SX, SY, SZ).y, EPS, "sinking should stop at the floor");

        // Look straight down and scroll into the floor: the rail stops there too
        camera.reset();
        camera.look(0, 1000);
        for (int i = 0; i < 500; i++)
            camera.dolly(1, SX, SY, SZ);
        assertEquals(0f, camera.position(SX, SY, SZ).y, EPS);
    }

    @Test
    void cannotFlyOffIntoTheDistance() {
        final var box = PreviewCamera.bounds(SX, SY, SZ);
        final int[][] directions = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {1, 1, 1}};
        for (int[] d : directions) {
            final var camera = new PreviewCamera();
            camera.look(37, 0);
            camera.move(d[0], d[1], d[2], 600f, SX, SY, SZ);
            final Vector3f pos = camera.position(SX, SY, SZ);
            assertTrue(pos.x >= box[0].x - EPS && pos.x <= box[1].x + EPS
                    && pos.y >= box[0].y - EPS && pos.y <= box[1].y + EPS
                    && pos.z >= box[0].z - EPS && pos.z <= box[1].z + EPS, "escaped to " + pos);
        }

        // ...and the box is the room grown by a few blocks, not unbounded
        assertTrue(box[1].y <= Math.max(SY, new PreviewCamera().position(SX, SY, SZ).y) + PreviewCamera.BOUNDS_BUFFER + EPS);
        assertEquals(0f, box[0].y, EPS);
    }

    @Test
    void defaultViewIsInsideTheBoundsAndUntouched() {
        final var camera = new PreviewCamera();
        final Vector3f home = camera.position(SX, SY, SZ);
        final var box = PreviewCamera.bounds(SX, SY, SZ);
        assertTrue(home.x > box[0].x && home.x < box[1].x && home.y > box[0].y && home.y < box[1].y
                && home.z > box[0].z && home.z < box[1].z, "default view outside bounds: " + home);

        // A no-op nudge doesn't drag it anywhere
        camera.dolly(0, SX, SY, SZ);
        assertTrue(home.equals(camera.position(SX, SY, SZ), EPS));
        assertStraightAhead(camera, center());
    }

    @Test
    void resetReturnsToTheDefaultRoomView() {
        final var camera = new PreviewCamera();
        final Vector3f initial = camera.position(SX, SY, SZ);

        camera.move(1, 1, 1, 2f, SX, SY, SZ);
        camera.look(300, 50);
        camera.dolly(5, SX, SY, SZ);
        camera.reset();

        assertTrue(initial.equals(camera.position(SX, SY, SZ), EPS));
        assertStraightAhead(camera, center());
    }

    @Test
    void centreRayLooksWhereTheCameraLooks() {
        final float yaw = 0.7f, pitch = -0.3f;
        final Vector3f ray = PreviewCamera.ray(0f, 0f, 1.6f, yaw, pitch);
        final Vector3f forward = PreviewCamera.backward(yaw, pitch).negate();
        assertEquals(forward.x, ray.x, 1e-4f);
        assertEquals(forward.y, ray.y, 1e-4f);
        assertEquals(forward.z, ray.z, 1e-4f);
    }

    @Test
    void rayPassesThroughItsScreenPoint() {
        final var camera = new PreviewCamera();
        camera.look(40, -25);
        final float aspect = 1.5f, ndcX = 0.6f, ndcY = -0.8f;
        final float tan = (float) Math.tan(Math.toRadians(RoomPreviewGuiRenderer.FOV_DEGREES / 2f));

        final Vector3f ray = PreviewCamera.ray(ndcX, ndcY, aspect, camera.yaw(), camera.pitch());
        final Vector3f e = eye(camera, camera.position(SX, SY, SZ).fma(10f, ray));
        assertTrue(e.z < 0f, "should be in front of the camera: " + e);
        assertEquals(ndcX, e.x / -e.z / (tan * aspect), 1e-4f);
        assertEquals(ndcY, e.y / -e.z / tan, 1e-4f);
    }
}
