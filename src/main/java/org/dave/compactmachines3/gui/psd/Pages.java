package org.dave.compactmachines3.gui.psd;

import net.minecraft.item.ItemStack;
import org.dave.compactmachines3.gui.psd.segments.ChaptersSegment;
import org.dave.compactmachines3.gui.psd.segments.ImageSegment;
import org.dave.compactmachines3.gui.psd.segments.TextSegment;
import org.dave.compactmachines3.gui.psd.segments.VerticalSpaceSegment;
import org.dave.compactmachines3.init.Blockss;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Pages {
    private final Map<String, Page> pages;
    private String activePage;

    public Pages() {
        this.pages = new HashMap<>();
        this.activePage = "welcome";

        Page welcomePage = new Page(this, "welcome");
        welcomePage.addSegment(new TextSegment("welcome"));
        welcomePage.addSegment(new VerticalSpaceSegment(11));
        welcomePage.addSegment(new TextSegment("chapters"));

        ChaptersSegment chapters = new ChaptersSegment(this);
        chapters.addChapter(new ItemStack(Blockss.machine, 1), "machines");
        chapters.addChapter(new ItemStack(Blockss.tunnel, 1), "tunnels");
        chapters.addChapter(new ItemStack(Blockss.redstoneTunnel, 1), "redstone_tunnels");
        chapters.addChapter(new ItemStack(Blockss.fieldProjector, 1), "crafting");

        welcomePage.addSegment(chapters);

        registerPage(welcomePage);
        registerPage(new SimpleTextPage(this, "tunnels"));
        registerPage(new SimpleTextPage(this, "machines"));
        registerPage(new SimpleTextPage(this, "redstone_tunnels"));

        Page craftingPage = new Page(this, "crafting");
        craftingPage.addSegment(new TextSegment("text"));
        craftingPage.addSegment(new VerticalSpaceSegment(-4));

        ImageSegment craftingExampleImage = new ImageSegment("compactmachines3:textures/gui/field_example.png", 50, 50);
        craftingExampleImage.setTextureWidth(256);
        craftingExampleImage.setTextureHeight(256);
        craftingExampleImage.setCentered(true);

        craftingPage.addSegment(craftingExampleImage);
        registerPage(craftingPage);
    }

    public void registerPage(Page page) {
        pages.put(page.getName(), page);
    }

    public Page getActivePage() {
        return pages.get(activePage);
    }

    public Set<Page> getPages() {
        return new HashSet<>(pages.values());
    }

    public void setActivePage(Page page) {
        activePage = page.getName();
    }

    public void setActivePage(String page) {
        if (pages.containsKey(page))
            activePage = page;
    }
}
