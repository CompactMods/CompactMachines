package dev.compactmods.machines.client.render;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;

public class ConditionalGhostSlot extends Slot {
    public ConditionalGhostSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    @Override
    public String toString() {
        return "GhostSlot {%s}".formatted(this.container.getItem(this.index).toString());
    }
}
