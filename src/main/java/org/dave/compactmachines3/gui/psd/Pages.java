package org.dave.compactmachines3.gui.psd;

import net.minecraft.item.ItemStack;
import org.dave.compactmachines3.gui.psd.segments.ChaptersSegment;
import org.dave.compactmachines3.gui.psd.segments.ImageSegment;
import org.dave.compactmachines3.gui.psd.segments.TextSegment;
import org.dave.compactmachines3.gui.psd.segments.VerticalSpaceSegment;
import org.dave.compactmachines3.init.Blockss;

import java.util.HashMap;

public class Pages {
    private HashMap<String, Page> pages;
    public static String activePageOnClient = "welcome";

    public Pages() {
        pages = new HashMap<>();

        Page welcomePage = new Page("welcome");
        welcomePage.addSegment(new TextSegment("welcome"));
        welcomePage.addSegment(new VerticalSpaceSegment(11));
        welcomePage.addSegment(new TextSegment("chapters"));

        ChaptersSegment chapters = new ChaptersSegment();
        // TODO: Chapter labels should be localized
        chapters.addChapter(new ItemStack(Blockss.machine, 1), "Compact Machines", "machines");
        chapters.addChapter(new ItemStack(Blockss.tunnel, 1), "Tunnels", "tunnels");
        chapters.addChapter(new ItemStack(Blockss.fieldProjector, 1), "Miniaturization Crafting", "crafting");

        welcomePage.addSegment(chapters);

        registerPage(welcomePage);
        registerPage(new SimpleTextPage("tunnels"));
        registerPage(new SimpleTextPage("machines"));

        Page craftingPage = new Page("crafting");
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
        return pages.get(activePageOnClient);
    }
}
