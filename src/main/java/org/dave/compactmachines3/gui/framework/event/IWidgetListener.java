package org.dave.compactmachines3.gui.framework.event;


import org.dave.compactmachines3.gui.framework.widgets.Widget;

public interface IWidgetListener<T extends IEvent> {
    WidgetEventResult call(T event, Widget widget);
}
