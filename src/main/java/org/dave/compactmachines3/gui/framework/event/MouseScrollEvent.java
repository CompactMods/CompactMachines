package org.dave.compactmachines3.gui.framework.event;

public class MouseScrollEvent implements IEvent {
    public boolean up = false;
    public boolean down = false;
    public int rawValue;

    public MouseScrollEvent(int rawValue) {
        this.rawValue = rawValue;
        if(rawValue < 0) {
            this.down = true;
        } else {
            this.up = true;
        }
    }

    @Override
    public String toString() {
        return String.format("MouseScroll[value=%d]", this.rawValue);
    }
}
