package org.dave.compactmachines3.gui.framework.widgets;



import org.dave.compactmachines3.gui.framework.CircularPointedArrayList;
import org.dave.compactmachines3.gui.framework.event.MouseClickEvent;
import org.dave.compactmachines3.gui.framework.event.ValueChangedEvent;
import org.dave.compactmachines3.gui.framework.event.WidgetEventResult;

import java.util.Collection;

public class WidgetWithChoiceValue<T> extends Widget {
    CircularPointedArrayList<T> choices;

    public WidgetWithChoiceValue() {
        choices = new CircularPointedArrayList<>();
    }

    public T getValue() {
        return this.choices.getPointedElement();
    }

    public void setValue(T choice) {
        choices.setPointerTo(choice);
    }

    public void addChoice(T... newChoices) {
        for(T newChoice : newChoices) {
            this.choices.add(newChoice);
        }
    }

    public void addChoiceFromArray(T[] newChoices) {
        for(T newChoice : newChoices) {
            this.choices.add(newChoice);
        }
    }

    public void addChoice(Collection<T> newChoices) {
        this.choices.addAll(newChoices);
    }

    public void next() {
        T oldValue = choices.getPointedElement();
        T newValue = choices.next();
        this.fireEvent(new ValueChangedEvent<T>(oldValue, newValue));
    }

    public void prev() {
        T oldValue = choices.getPointedElement();
        T newValue = choices.prev();
        this.fireEvent(new ValueChangedEvent<T>(oldValue, newValue));
    }

    public void addClickListener() {
        this.addListener(MouseClickEvent.class, (event, widget) -> {
            if(event.isLeftClick()) {
                ((WidgetWithChoiceValue<T>)widget).next();
            } else {
                ((WidgetWithChoiceValue<T>)widget).prev();
            }

            return WidgetEventResult.HANDLED;
        });
    }
}
