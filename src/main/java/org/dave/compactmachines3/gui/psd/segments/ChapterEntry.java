package org.dave.compactmachines3.gui.psd.segments;

import net.minecraft.item.ItemStack;

public class ChapterEntry {
    private ItemStack stack;
    private String label;
    private String targetPage;

    public ChapterEntry(ItemStack stack, String label, String targetPage) {
        this.stack = stack;
        this.label = label;
        this.targetPage = targetPage;
    }

    public ItemStack getStack() {
        return stack;
    }

    public String getLabel() {
        return label;
    }

    public String getTargetPage() {
        return targetPage;
    }
}
