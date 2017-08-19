package org.dave.compactmachines3.gui.psd;

import org.dave.compactmachines3.gui.psd.segments.TextSegment;

public class SimpleTextPage extends Page {

    public SimpleTextPage(String name) {
        super(name);
        this.addSegment(new TextSegment("text"));
    }

    public SimpleTextPage(String name, String textId) {
        super(name);
        this.addSegment(new TextSegment(textId));
    }
}
