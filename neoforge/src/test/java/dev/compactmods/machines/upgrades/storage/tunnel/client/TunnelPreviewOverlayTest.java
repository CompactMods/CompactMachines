package dev.compactmods.machines.upgrades.storage.tunnel.client;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TunnelPreviewOverlayTest {

    private static final Vector3f CENTER = new Vector3f(5, 0, 0);

    @Test
    void hitsTheNearSurface() {
        assertEquals(4f, TunnelPreviewOverlay.raySphere(new Vector3f(), new Vector3f(1, 0, 0), CENTER, 1f), 1e-5f);
    }

    @Test
    void missesBesideAndBehind() {
        assertEquals(-1f, TunnelPreviewOverlay.raySphere(new Vector3f(), new Vector3f(0, 1, 0), CENTER, 1f));
        assertEquals(-1f, TunnelPreviewOverlay.raySphere(new Vector3f(), new Vector3f(-1, 0, 0), CENTER, 1f));
    }

    @Test
    void fromInsideIsAtZero() {
        assertEquals(0f, TunnelPreviewOverlay.raySphere(new Vector3f(5.5f, 0, 0), new Vector3f(0, 0, 1), CENTER, 1f));
    }
}
