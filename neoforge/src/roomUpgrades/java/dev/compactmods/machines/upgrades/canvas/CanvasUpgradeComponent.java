package dev.compactmods.machines.upgrades.canvas;

import dev.compactmods.machines.upgrades.api.RoomUpgradeComponent;

/**
 * A {@link RoomUpgradeComponent} with nodes on the room's upgrade canvas. While an upgrade with such a
 * component is enabled on a room, the room's canvas offers the component's nodes; the component
 * keeps whatever it needs from them (through {@link RoomCanvasContext#watch watches}) in its own
 * state — the canvas holds no upgrade data.
 */
public interface CanvasUpgradeComponent extends RoomUpgradeComponent {

    /** Adds this component's nodes and watches. Called each time the room's canvas is built. */
    void contribute(RoomCanvasContext canvas);
}
