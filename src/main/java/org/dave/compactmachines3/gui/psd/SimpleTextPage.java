package org.dave.compactmachines3.gui.psd;

import org.dave.compactmachines3.gui.psd.segments.TextSegment;

public class SimpleTextPage extends Page {
    public SimpleTextPage(Pages pages, String name) {
        super(pages, name);
        this.addSegment(new TextSegment("text"));
    }

    public SimpleTextPage(Pages pages, String name, String textId) {
        super(pages, name);
        this.addSegment(new TextSegment(textId));
    }
}
