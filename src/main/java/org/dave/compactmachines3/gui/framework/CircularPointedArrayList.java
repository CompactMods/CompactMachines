package org.dave.compactmachines3.gui.framework;

import java.util.ArrayList;

public class CircularPointedArrayList<E> extends ArrayList<E> {
    int pointer = 0;

    public E getPointedElement() {
        return this.get(pointer);
    }

    public E next() {
        this.pointer++;
        if(pointer >= this.size()) {
            pointer = 0;
        }

        return getPointedElement();
    }

    public E prev() {
        this.pointer--;
        if(pointer <= 0) {
            pointer = this.size()-1;
        }

        return getPointedElement();
    }

    public void setPointerTo(E element) {
        int position = this.indexOf(element);
        if(position == -1) {
            pointer = 0;
        } else {
            pointer = position;
        }
    }
}
