package org.dave.compactmachines3.gui.framework.event;

public class MouseClickEvent implements IEvent {
    public int button;
    public int x;
    public int y;

    public MouseClickEvent(int mouseX, int mouseY, int button) {
        this.x = mouseX;
        this.y = mouseY;
        this.button = button;
    }

    public boolean isLeftClick() { return button == 0; }

    @Override
    public String toString() {
        return String.format("MouseClick[x=%d,y=%d,button=%d]", this.x, this.y, this.button);
    }
}
