package org.dave.compactmachines3.gui.framework.event;

public class KeyTypedEvent implements IEvent {
    public char typedChar;
    public int keyCode;

    public KeyTypedEvent(char typedChar, int keyCode) {
        this.typedChar = typedChar;
        this.keyCode = keyCode;
    }
}
