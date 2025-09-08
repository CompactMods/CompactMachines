package dev.compactmods.machines.client.render;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ConditionalGhostSlot extends Slot {
    public ConditionalGhostSlot(Container pContainer, int pSlot, int pX, int pY) {
        super(pContainer, pSlot, pX, pY);
    }

    public boolean matched(ItemStack stack) {
        return true;
    }

    @Override
    public boolean mayPickup(Player pPlayer) {
        return matched(getItem());
    }

    @Override
    public boolean isHighlightable() {
        final var stack = getItem();
        return matched(stack);
    }

    @Override
    public String toString() {
        return "GhostSlot {%s}".formatted(this.container.getItem(this.index).toString());
    }
}
