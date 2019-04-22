package org.dave.compactmachines3.gui.framework.widgets;

import org.dave.compactmachines3.gui.framework.event.ValueChangedEvent;

public class WidgetWithValue<T> extends Widget {
    T value;

    public T getValue() {
        return this.value;
    }

    public void setValue(T newValue) {
        T tmpVal = this.value;
        this.value = newValue;
        this.fireEvent(new ValueChangedEvent<T>(tmpVal, this.value));
    }

    public void valueChanged(T oldValue, T newValue) {
        this.value = newValue;
        this.fireEvent(new ValueChangedEvent<T>(oldValue, this.value));
    }
}
