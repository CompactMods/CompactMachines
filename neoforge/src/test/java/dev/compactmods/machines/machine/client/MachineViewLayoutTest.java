package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.machine.client.MachineViewLayout.View;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MachineViewLayoutTest {

    private static final ScreenRectangle AREA = new ScreenRectangle(5, 20, 300, 200);

    @Test
    void previewAloneIsCentredAtTwoThirds() {
        final var layout = MachineViewLayout.tile(EnumSet.of(View.ROOM_PREVIEW), AREA);
        assertEquals(new ScreenRectangle(55, 53, 200, 133), layout.get(View.ROOM_PREVIEW));
    }

    @Test
    void canvasAloneFillsTheArea() {
        final var layout = MachineViewLayout.tile(EnumSet.of(View.MACHINE_CONFIG), AREA);
        assertEquals(AREA, layout.get(View.MACHINE_CONFIG));
    }

    @Test
    void allThreeTileWithoutOverlapping() {
        final var layout = MachineViewLayout.tile(EnumSet.allOf(View.class), AREA);
        final var upgrades = layout.get(View.ROOM_UPGRADES);
        final var config = layout.get(View.MACHINE_CONFIG);
        final var preview = layout.get(View.ROOM_PREVIEW);

        // Preview on the right third, full height; canvases stacked on the left
        assertEquals(AREA.right(), preview.right());
        assertEquals(AREA.height(), preview.height());
        assertEquals(upgrades.width(), config.width());
        assertTrue(upgrades.top() < config.top());

        assertFalse(upgrades.overlaps(config));
        assertFalse(upgrades.overlaps(preview));
        assertFalse(config.overlaps(preview));
        assertTrue(config.bottom() <= AREA.bottom());
    }
}
