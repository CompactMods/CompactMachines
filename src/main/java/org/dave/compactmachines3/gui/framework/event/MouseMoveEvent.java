package org.dave.compactmachines3.gui.framework.event;

public class MouseMoveEvent implements IEvent {
    public int x;
    public int y;

    public MouseMoveEvent(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("MouseMove[x=%d,y=%d]", this.x, this.y);
    }
}
