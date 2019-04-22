package org.dave.compactmachines3.gui.framework.event;

public class MouseClickMoveEvent implements IEvent {
    public int button;
    public int x;
    public int y;
    public long timeSinceLastClick;

    public MouseClickMoveEvent(int mouseX, int mouseY, int button, long timeSinceLastClick) {
        this.x = mouseX;
        this.y = mouseY;
        this.button = button;
        this.timeSinceLastClick = timeSinceLastClick;
    }

    public boolean isLeftClick() { return button == 0; }

    @Override
    public String toString() {
        return String.format("MouseClickMove[x=%d,y=%d,button=%d,timeSinceLastClick=%d]", this.x, this.y, this.button, this.timeSinceLastClick);
    }
}
