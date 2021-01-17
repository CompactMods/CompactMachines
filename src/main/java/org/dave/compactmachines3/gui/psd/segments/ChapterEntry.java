package org.dave.compactmachines3.gui.psd.segments;

import net.minecraft.item.ItemStack;

public class ChapterEntry {
    private final ItemStack stack;
    private final String targetPage;
    private final String langKey;

    public ChapterEntry(ItemStack stack, String targetPage) {
        this.stack = stack;
        this.targetPage = targetPage;
        this.langKey = "gui.compactmachines3.psd." + targetPage + ".label";
    }

    public ItemStack getStack() {
        return stack;
    }

    public String getTargetPage() {
        return targetPage;
    }

    public String getLangKey() {
        return langKey;
    }
}
