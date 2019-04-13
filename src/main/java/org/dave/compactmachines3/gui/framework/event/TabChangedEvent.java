package org.dave.compactmachines3.gui.framework.event;

import org.dave.compactmachines3.gui.framework.widgets.WidgetPanel;

public class TabChangedEvent extends ValueChangedEvent<WidgetPanel> {
    public TabChangedEvent(WidgetPanel oldValue, WidgetPanel newValue) {
        super(oldValue, newValue);
    }
}
