package org.dave.cm2.gui;

import net.minecraft.item.ItemStack;

public abstract class GuiPSDSelectorEntry {
    private ItemStack stack;
    private String text;

    public GuiPSDSelectorEntry(ItemStack stack, String text) {
        this.stack = stack;
        this.text = text;
    }

    public ItemStack getStack() {
        return stack;
    }

    public String getText() {
        return text;
    }

    public abstract void performAction();
}
